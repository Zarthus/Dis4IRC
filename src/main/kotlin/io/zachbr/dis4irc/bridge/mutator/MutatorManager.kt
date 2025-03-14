/*
 * This file is part of Dis4IRC.
 *
 * Copyright (c) 2018-2019 Dis4IRC contributors
 *
 * MIT License
 */

package io.zachbr.dis4irc.bridge.mutator

import io.zachbr.dis4irc.bridge.Bridge
import io.zachbr.dis4irc.bridge.message.Message
import io.zachbr.dis4irc.bridge.mutator.api.Mutator
import io.zachbr.dis4irc.bridge.mutator.mutators.BlockHereEveryone
import io.zachbr.dis4irc.bridge.mutator.mutators.PasteLongMessages
import io.zachbr.dis4irc.bridge.mutator.mutators.TranslateFormatting
import ninja.leaping.configurate.commented.CommentedConfigurationNode

class MutatorManager(bridge: Bridge, config: CommentedConfigurationNode) {
    private val mutators = ArrayList<Mutator>()

    init {
        registerMutator(BlockHereEveryone())
        registerMutator(PasteLongMessages(bridge, config.getNode("paste-service")))
        registerMutator(TranslateFormatting())
    }

    private fun registerMutator(mutator: Mutator) {
        mutators.add(mutator)
    }

    internal fun applyMutators(message: Message): Message? {
        val iterator = mutators.iterator()

        loop@ while (iterator.hasNext()) {
            val mutator = iterator.next()
            if (message.hasAlreadyApplied(mutator.javaClass)) {
                continue
            }

            val state = mutator.mutate(message)
            message.markMutatorApplied(mutator.javaClass)

            return when (state) {
                Mutator.LifeCycle.CONTINUE -> continue@loop
                Mutator.LifeCycle.STOP_AND_DISCARD -> null
                Mutator.LifeCycle.RETURN_EARLY -> message
            }
        }

        return message
    }
}
