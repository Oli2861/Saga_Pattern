package com.oli.event

import com.rabbitmq.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture


interface MessageBroker {
    suspend fun remoteProcedureCall(queueName: String, replyQueueName: String, event: Event): String?
    fun listenForRPC(scope: CoroutineScope, queueName: String, onReceive: suspend (String, Event) -> Event, onCancel: () -> Unit)
}

object RabbitMQBroker : MessageBroker {
    private val CONNECTION_INSTANCE: Connection
        get() = establishConnection(System.getenv("RABBITMQ_HOST"))

    private val logger: Logger = LoggerFactory.getLogger(RabbitMQBroker::class.java)

    private var OPEN_CHANNELS: MutableMap<Int, Channel> = mutableMapOf()
    private val declaredQueues: MutableSet<String> = mutableSetOf()

    private val defaultExchange: String = ""
    private const val PUBLISH_CHANNEL_ID: Int = 1
    private const val RECEIVE_CHANNEL_ID: Int = 2

    private fun establishConnection(host: String): Connection {
        val factory = ConnectionFactory()
        factory.host = host
        return factory.newConnection()
    }

    private fun getChannel(channelId: Int): Channel {
        logger.debug("RABBITMQ_HOST: ${System.getenv("RABBITMQ_HOST")}")
        if (OPEN_CHANNELS[channelId] == null) {
            val channel = CONNECTION_INSTANCE.createChannel(channelId)
            OPEN_CHANNELS[channelId] = channel
        }
        return OPEN_CHANNELS[channelId]!!
    }

    private fun createQueueIfDoesNotExist(queueName: String, channel: Channel) {
        if (!declaredQueues.contains(queueName)) {
            channel.queueDeclare(queueName, false, false, false, null)
            declaredQueues.add(queueName)
        }
    }

    override suspend fun remoteProcedureCall(queueName: String, replyQueueName: String, event: Event): String? {
        val message = EventSerializer.serialize(event)
        val channel = getChannel(PUBLISH_CHANNEL_ID)
        createQueueIfDoesNotExist(queueName, channel)
        val replyChannel = getChannel(RECEIVE_CHANNEL_ID)
        createQueueIfDoesNotExist(replyQueueName, replyChannel)

        val correlationId = UUID.randomUUID().toString()
        val properties = AMQP.BasicProperties.Builder().correlationId(correlationId).replyTo(replyQueueName).build()
        channel.basicPublish(defaultExchange, queueName, properties, message.toByteArray())
        val response = CompletableFuture<String>();
        val consumerTag = channel.basicConsume(replyQueueName, true,
            { consumerTag: String?, delivery: Delivery ->
                if (delivery.properties.correlationId == correlationId) {
                    response.complete(String(delivery.body, Charsets.UTF_8))
                }
            }
        ) { consumerTag: String? -> }
        val result = response.get()
        channel.basicCancel(consumerTag)
        return result
    }

    override fun listenForRPC(
        scope: CoroutineScope,
        queueName: String,
        onReceive: suspend (String, Event) -> Event,
        onCancel: () -> Unit
    ) {
        val channel = getChannel(RECEIVE_CHANNEL_ID)
        channel.queueDeclare(queueName, false, false, false, null)

        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            scope.launch {
                val replyProps = AMQP.BasicProperties.Builder()
                    .correlationId(delivery.properties.correlationId)
                    .build()
                val response = try {
                    val contents = String(delivery.body, Charsets.UTF_8)
                    val event = EventSerializer.deserialize(contents)
                    EventSerializer.serialize(onReceive(delivery.properties.correlationId, event))
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    EventSerializer.serialize(ErrorEvent(e.stackTraceToString()))
                }

                channel.basicPublish(defaultExchange, delivery.properties.replyTo, replyProps, response.toByteArray(Charsets.UTF_8))
                channel.basicAck(delivery.envelope.deliveryTag, false)
            }
        }
        channel.basicConsume(queueName, false, deliverCallback, { consumerTag -> onCancel })
    }

}
