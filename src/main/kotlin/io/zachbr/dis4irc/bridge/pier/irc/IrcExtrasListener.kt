/*
 * This file is part of Dis4IRC.
 *
 * Copyright (c) 2018-2019 Dis4IRC contributors
 *
 * MIT License
 */

package io.zachbr.dis4irc.bridge.pier.irc

import io.zachbr.dis4irc.bridge.message.BOT_SENDER
import io.zachbr.dis4irc.bridge.message.Message
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.channel.ChannelModeEvent
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent

class IrcExtrasListener(private val pier: IrcPier) {
    private val logger = pier.logger

    @Handler
    fun onModeChange(event: ChannelModeEvent) {
        val receiveTimestamp = System.nanoTime()
        val sender = BOT_SENDER
        val msgContent = "${event.actor.name} changed channel modes: ${event.statusList.asString}"
        logger.debug("IRC MODE CHANGE ${event.channel}")

        val source = event.channel.asBridgeSource()
        val message = Message(msgContent, sender, source, receiveTimestamp)
        pier.sendToBridge(message)
    }

    @Handler
    fun onTopicChange(event: ChannelTopicEvent) {
        val receiveTimestamp = System.nanoTime()
        val sender = BOT_SENDER
        var topicSetter = ""
        if (event.newTopic.setter.isPresent) {
            topicSetter = " set by " + event.newTopic.setter.get().name
        }
        val topicValue = event.newTopic.value.orElse("Unknown topic")
        val msgContent = "Topic$topicSetter: $topicValue"
        logger.debug("IRC TOPIC$topicSetter: $topicValue")

        val source = event.channel.asBridgeSource()
        val message = Message(msgContent, sender, source, receiveTimestamp)
        pier.sendToBridge(message)
    }
}
