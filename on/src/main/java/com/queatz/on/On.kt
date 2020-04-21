package com.queatz.on

import java.util.*
import kotlin.reflect.KClass

class On constructor(private val parent: On? = null) {

    private val members = HashMap<KClass<*>, Any>()
    private val membersExternal = HashMap<KClass<*>, Any>()

    inline operator fun <reified T : Any> invoke(): T = inject(T::class)
    inline operator fun <reified T : Any> invoke(block: T.() -> Unit): T = inject(T::class).also(block)

    fun off() {
        members.forEach {
            it.value.apply { if (this is OnLifecycle) off() }
        }
    }

    inline fun <reified T : Any> use(): T = inject(T::class, true)
    inline fun <reified T : Any> use(block: T.() -> Unit): T  = inject(T::class, true).also(block)

    inline fun <reified T : Any> use(member: T): T = injectMember(T::class, member)
    inline fun <reified T : Any> use(member: T, block: T.() -> Unit): T = injectMember(T::class, member).also(block)

    fun <T : Any> injectMember(clazz: KClass<T>, member: T): T {
        membersExternal[clazz] = member
        return member
    }

    fun <T : Any> inject(member: KClass<T>, local: Boolean = false): T {
        return when (member) {
            in members -> members[member] as T
            in if (local) emptyMap() else membersExternal -> membersExternal[member] as T
            in if (local) emptyMap() else parent?.members ?: emptyMap() -> parent!!.members[member] as T
            in if (local) emptyMap() else parent?.membersExternal ?: emptyMap() -> parent!!.membersExternal[member] as T
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