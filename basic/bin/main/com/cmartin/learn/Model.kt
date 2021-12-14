package com.cmartin.learn

object Model {
    @JvmInline
    value class Code(val value: String)

    @JvmInline
    value class Name(val value: String)

    data class Country(private val _code: Code, private val _name: Name) {
        val code: String = _code.value
        val name: String = _name.value
    }
}