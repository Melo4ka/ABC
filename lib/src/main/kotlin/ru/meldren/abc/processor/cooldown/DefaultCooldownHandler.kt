package ru.meldren.abc.processor.cooldown

import ru.meldren.abc.annotation.Cooldown
import ru.meldren.abc.common.CommandData
import ru.meldren.abc.processor.CooldownHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.reflect.KClass

class DefaultCooldownHandler<S : Any, C : Any, K : Any>(private val uuidResolver: (S) -> K) : CooldownHandler<S, C> {

    private val cooldowns = ConcurrentHashMap<K, ConcurrentHashMap<KClass<out Any>, Long>>()
    private val cooldownPool = Executors.newSingleThreadScheduledExecutor()

    override fun test(sender: S, commandData: CommandData<C>, annotation: Cooldown): Long {
        if (annotation.value == 0UL) {
            return 0
        }
        val commandClass = commandData.instance::class
        val cooldown = annotation.value.toLong().takeUnless { it < 0 } ?: Long.MAX_VALUE
        val uuid = uuidResolver(sender)
        val senderCooldowns = cooldowns.getOrPut(uuid, ::ConcurrentHashMap)
        val current = senderCooldowns[commandClass]
        if (current == null) {
            senderCooldowns[commandClass] = System.currentTimeMillis()
            cooldownPool.schedule({
                senderCooldowns.remove(commandClass)
            }, cooldown, annotation.unit)
            return 0
        }
        val left = annotation.unit.toMillis(cooldown) - (System.currentTimeMillis() - current)
        return max(1000, left)
    }
}