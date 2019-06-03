package com.chenan.mvvm.test

import com.chenan.mvvm.util.PluginHelper
import com.chenan.mvvm.util.Utils
import java.util.regex.Pattern

fun main() {
    val text = "package java.com.chenan.test.activity\n" +
            "\n" +
            "interface Test0{\n" +
            "    interface Text1{\n" +
            "\n" +
            "    }\n" +
            "    interface Text2{\n" +
            "        \n" +
            "    }\n" +
            "    interface Text3{\n" +
            "        \n" +
            "    }\n" +
            "}"
    val p = Pattern.compile("interface\\s(\\w*)")
    p.matcher(text)?.let {
        while (it.find()) {
            println("find ${it.groupCount()}")
            println(it.group(1))
        }
    } ?: println("null find")
}