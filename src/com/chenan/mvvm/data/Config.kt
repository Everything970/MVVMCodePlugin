package com.chenan.mvvm.data

import com.chenan.mvvm.util.Utils
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class Config {
    var lateActivity: String = Utils.defaultActivity
    var lateViewModel: String = Utils.defaultViewModel
    var lateLayout: String = Utils.defaultLayout

    fun setActivity(str: String?) {
        if (!str.isNullOrEmpty()) lateActivity = str
    }

    fun setViewModel(str: String?) {
        if (!str.isNullOrEmpty()) lateViewModel = str
    }

    fun setLayout(str: String?) {
        if (!str.isNullOrEmpty()) lateLayout = str
    }

    override fun toString(): String {
        return "Config(lateActivity='$lateActivity', lateViewModel='$lateViewModel', lateLayout='$lateLayout')"
    }

    companion object {
        fun getInstance(): Config {
            val file = File(Utils.getPluginPath(), "config.txt")
            return if (!file.exists()) {
                Config()
            } else {
                try {
                    val fis = FileInputStream(file)
                    val isr = InputStreamReader(fis, Charsets.UTF_8)
                    val br = BufferedReader(isr)
                    val json = StringBuffer()
                    while (true) {
                        val text = br.readLine() ?: break
                        json.append(text)
                    }
                    Gson().fromJson<Config>(json.toString(), Config::class.java)
                } catch (e: Exception) {
                    Config()
                }
            }
        }
    }
}