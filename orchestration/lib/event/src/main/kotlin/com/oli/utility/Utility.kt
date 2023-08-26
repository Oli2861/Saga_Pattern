package com.oli.utility

import com.oli.event.ErrorEvent
import com.oli.serialization.EventSerializer
import com.oli.event.ReplyEvent
import org.slf4j.Logger

/**
 * Evaluate a received reply, decode it and evaluate its result.
 */
fun evaluateReply(reply: String, logger: Logger): Boolean {
    return when(val replyEvent = EventSerializer.deserialize(reply)){
        is ReplyEvent -> replyEvent.result
        is ErrorEvent -> {
            logger.debug("Received error event $replyEvent.")
            false
        }
        else -> {
            logger.debug("Received unexpected reply event $replyEvent.")
            false
        }
    }
}
