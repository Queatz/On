package com.queatz.on

import java.util.*
import kotlin.reflect.KClass

class On {

    private val members = HashMap<KClass<*>, Any>()
    private val membersExternal = HashMap<KClass<*>, Any>()

    inline operator fun <reified T : Any> invoke(): T { return inject(T::class) }
    inline operator fun <reified T : Any> invoke(block: T.() -> Unit): T { return inject(T::class) }

    fun off() {
        members.forEach {
            it.value.apply { if (this is OnLifecycle) off() }
        }
    }

    inline fun <reified T : Any> use(member: T) { injectMember(T::class, member) }

    fun <T : Any> injectMember(clazz: KClass<T>, member: T) {
        membersExternal[clazz] = member
    }

    fun <T : Any> inject(member: KClass<T>): T {
        return when (member) {
            in members -> members[member] as T
            in membersExternal -> membersExternal[member] as T
            else -> {
                val instance = member.java.getConstructor(On::class.java).newInstance(this)
                members[member] = instance
                if (instance is OnLifecycle) { instance.on() }
                instance
            }
        }
    }
}

interface OnLifecycle {
    fun on() {}
    fun off() {}
}