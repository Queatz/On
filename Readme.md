On&lt;Kotlin&gt;
================

A lightweight dependency injection library for Kotlin.

Usage
-----

Import:

    implementation 'com.github.Queatz:On:0.1.1'

Define an injectable:

    class Cat(private val on: On) {
        fun meow() {
            //...
        }
    }

An Injectable declares a single-parameter constructor that takes in an `On` type.

Next, define a scope:

    class Activity {
        private val on = On()
    }

Finally, inject!

    class Activity {
        //...

        fun meowButtonPressed() {
            on<Cat>().meow()
            on<Cat> { meow() } // or this
        }

    }

