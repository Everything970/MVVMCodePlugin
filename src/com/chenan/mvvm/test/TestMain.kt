package com.chenan.mvvm.test

import com.chenan.mvvm.util.PluginHelper
import com.chenan.mvvm.util.Utils

fun main() {
    val file = Utils.getPluginPath()
    println(file.absolutePath)
}