package com.queatz.on

import kotlin.reflect.KClass

class On constructor(private val parent: On? = null) {

    private val members = mutableMapOf<KClass<*>, Any>()
    private val membersExternal = mutableMapOf<KClass<*>, Any>()

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
        return search(member, local) ?: member.java.getConstructor(On::class.java).newInstance(this).apply {
            members[member] = this
            if (this is OnLifecycle) { on() }
        }
    }

    fun <T : Any> search(member: KClass<T>, local: Boolean = false): T? {
        return when (member) {
            in members -> members[member] as T
            in membersExternal -> membersExternal[member] as T
            else -> if (!local) parent?.search(member) else null
        }
    }
}

interface OnLifecycle {
    fun on() {}
    fun off() {}
}