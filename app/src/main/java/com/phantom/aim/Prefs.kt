package com.phantom.aim

import android.content.Context

object Prefs {
    private const val NAME = "phantom_prefs"
    private fun sp(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var aim: Boolean
        get() = sp(ctx).getBoolean("aim", false)
        set(v) = sp(ctx).edit().putBoolean("aim", v).apply()

    var esp: Boolean
        get() = sp(ctx).getBoolean("esp", false)
        set(v) = sp(ctx).edit().putBoolean("esp", v).apply()

    var anti: Boolean
        get() = sp(ctx).getBoolean("anti", true)
        set(v) = sp(ctx).edit().putBoolean("anti", v).apply()

    private lateinit var ctx: Context
    fun init(c: Context) { ctx = c.applicationContext }
}
