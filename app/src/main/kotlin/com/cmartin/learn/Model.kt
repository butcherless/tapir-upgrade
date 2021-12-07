package com.cmartin.learn

object Model {
    @JvmInline
    value class CountryCode(private val code:String)
    data class Country(val code: String, val name: String)
}
