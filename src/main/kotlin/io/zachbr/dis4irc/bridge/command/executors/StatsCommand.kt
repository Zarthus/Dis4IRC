/*
 * This file is part of Dis4IRC.
 *
 * Copyright (c) 2018-2019 Dis4IRC contributors
 *
 * MIT License
 */

package io.zachbr.dis4irc.bridge.command.executors

import io.zachbr.dis4irc.bridge.Bridge
import io.zachbr.dis4irc.bridge.command.api.Executor
import io.zachbr.dis4irc.bridge.message.Message
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val EXEC_DELAY_MILLIS = 60_000

class StatsCommand(private val bridge: Bridge) : Executor {
    private var lastExecution = 0L

    private fun isRateLimited(): Boolean {
        val now = System.currentTimeMillis()

        return if (now - lastExecution > EXEC_DELAY_MILLIS) {
            lastExecution = now
            false
        } else {
            true
        }
    }

    override fun onCommand(command: Message): String? {
        if (isRateLimited()) {
            return null
        }

        val sortedTimings = bridge.statsManager.getMessageTimings().sortedArray()
        val meanMillis = TimeUnit.NANOSECONDS.toMillis(mean(sortedTimings))
        val medianMillis = TimeUnit.NANOSECONDS.toMillis(median(sortedTimings))

        val uptime = ManagementFactory.getRuntimeMXBean().uptime
        val uptimeStr = convertMillisToPretty(uptime)

        val fromIrc = bridge.statsManager.getTotalFromIrc()
        val fromDiscord = bridge.statsManager.getTotalFromDiscord()

        val fromIrcPercent = percent(fromIrc, fromDiscord + fromIrc)
        val fromDiscordPercent = 100 - fromIrcPercent

        return "Uptime: $uptimeStr\n" +
                "Message Handling: ${meanMillis}ms / ${medianMillis}ms (mean/median)\n" +
                "Messages from IRC: $fromIrc ($fromIrcPercent%)\n" +
                "Messages from Discord: $fromDiscord ($fromDiscordPercent%)"
    }

    private fun percent(value: Long, total: Long): Int {
        if (total == 0L || value == total) {
            return 100
        }

        return ((value * 100.0) / total).roundToInt()
    }

    /**
     * Gets the mean of a given array
     */
    private fun mean(a: LongArray): Long {
        var sum = 0L
        for (i in a.indices) {
            sum += a[i]
        }

        return sum / a.size
    }

    /**
     * Gets the median of a given sorted array
     */
    private fun median(a: LongArray): Long {
        val middle = a.size / 2

        return if (a.size % 2 == 1) {
            a[middle]
        } else {
            (a[middle - 1] + a[middle]) / 2
        }
    }

    /**
     * Converts the given amount of milliseconds to a presentable elapsed time string
     */
    private fun convertMillisToPretty(diffMillis: Long): String {
        var left = diffMillis

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays = left / daysInMilli
        left %= daysInMilli

        val elapsedHours = left / hoursInMilli
        left %= hoursInMilli

        val elapsedMinutes = left / minutesInMilli
        left %= minutesInMilli

        val elapsedSeconds = left / secondsInMilli

        return String.format("%d days, %d hours, %d minutes, %d seconds",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds)
    }
}
