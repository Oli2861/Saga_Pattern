package com.oli.broker

import com.oli.event.ErrorEvent
import com.oli.event.Event
import com.oli.serialization.EventSerializer
import com.rabbitmq.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture

class RabbitMQConnectionManager private constructor(
    private val host: String
) : MessageBroker {

    private val connectionInstance: Connection
        get() = establishConnection(host)
    private var openChannels: MutableMap<Int, Channel> = mutableMapOf()
    private val declaredQueues: MutableSet<String> = mutableSetOf()


    companion object {
        private const val RPC_PUBLISH_ID: Int = 1
        private const val RPC_RECEIVE_ID: Int = 2
        private const val REQUEST_CHANNEL_ID: Int = 3
        private const val REPLY_CHANNEL_ID: Int = 4
        private const val defaultExchange: String = ""
        private val logger: Logger = LoggerFactory.getLogger(RabbitMQConnectionManager::class.java)

        @Volatile
        private var INSTANCE: RabbitMQConnectionManager? = null

        fun getInstance(host: String): RabbitMQConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RabbitMQConnectionManager(host).also {
                    INSTANCE = it
                    logger.debug("RabbitMQBroker instance create with connection: ${INSTANCE!!.connectionInstance}.")
                }
            }
        }

    }


    private fun establishConnection(host: String): Connection {
        val factory = ConnectionFactory()
        factory.host = host
        return factory.newConnection()
    }

    /**
     * Get a channel from the connection. If the channel does not exist, create it.
     */
    private fun getChannel(channelId: Int): Channel {
        if (openChannels[channelId] == null) {
            val channel = connectionInstance.createChannel(channelId)
            openChannels[channelId] = channel
        }
        return openChannels[channelId]!!
    }

    /**
     * Create a queue if it does not exist.
     */
    private fun createQueueIfDoesNotExist(queueName: String, channel: Channel) {
        if (!declaredQueues.contains(queueName)) {
            channel.queueDeclare(queueName, false, false, false, null)
            declaredQueues.add(queueName)
        }
    }

    override suspend fun remoteProcedureCall(queueName: String, replyQueueName: String, event: Event): String? {
        val message = EventSerializer.serialize(event)
        val channel = getChannel(RPC_PUBLISH_ID)
        createQueueIfDoesNotExist(queueName, channel)
        val replyChannel = getChannel(RPC_RECEIVE_ID)
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
        val channel = getChannel(RPC_RECEIVE_ID)
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

                channel.basicPublish(
                    defaultExchange,
                    delivery.properties.replyTo,
                    replyProps,
                    response.toByteArray(Charsets.UTF_8)
                )
                channel.basicAck(delivery.envelope.deliveryTag, false)
            }
        }
        channel.basicConsume(queueName, false, deliverCallback, { consumerTag -> onCancel })
    }

    override suspend fun listenToRequestChannel(
        queueName: String
    ): SharedFlow<Event> {
        // Get channel, set prefetchCount to 1, create queue if it does not exist
        val channel = getChannel(REQUEST_CHANNEL_ID)
        channel.basicQos(1)
        createQueueIfDoesNotExist(queueName, channel)
        val flow: MutableSharedFlow<Event> = MutableSharedFlow()

        val autoAck = false
        channel.basicConsume(queueName, autoAck, "myConsumerTag",
            object : DefaultConsumer(channel) {
                @Throws(IOException::class)
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) {
                    val contents = String(body, Charsets.UTF_8)
                    val event = EventSerializer.deserialize(contents)

                    logger.info("Received event: $event")
                    // TODO: Currently blocks a thread of the RabbitMQ thread pool.
                    runBlocking {
                        flow.emit(event)
                    }

                    channel.basicAck(envelope.deliveryTag, false)
                }
            })

        return flow.asSharedFlow()
    }

    override suspend fun publishToQueue(queueName: String, event: Event) {
        // Get channel, create queue if it does not exist
        val channel = getChannel(REPLY_CHANNEL_ID)
        createQueueIfDoesNotExist(queueName, channel)
        // Serialize message, publish to queue
        val message = EventSerializer.serialize(event)
        channel.basicPublish(defaultExchange, queueName, null, message.toByteArray(Charsets.UTF_8))
    }

    override suspend fun closeConnection() {
        try {
            connectionInstance.close( 1000)
            logger.debug("Connection to $connectionInstance closed.")
        } catch (e: IOException) {
            logger.error("Error closing connection: $e")
        }
    }


}
