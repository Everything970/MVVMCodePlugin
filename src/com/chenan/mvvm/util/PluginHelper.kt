package com.chenan.mvvm.util

import com.chenan.mvvm.setting.MVVMSetting
import com.intellij.openapi.project.Project

class PluginHelper private constructor(
        private val project: Project
) {
    companion object {
        private var INSTANCE: PluginHelper? = null
        fun getInstance(project: Project): PluginHelper = INSTANCE ?: synchronized(PluginHelper::class.java) {
            INSTANCE ?: PluginHelper(project).also {
                INSTANCE = it
            }
        }
    }

    val setting = MVVMSetting.getInstance(project)

    val activitySet: HashSet<String> by lazy {
        hashSetOf<String>().also {
            it.add(Utils.defaultActivity)
            it.addAll(setting.activityMap.keys)
        }
    }

    val viewModelSet: HashSet<String> by lazy {
        hashSetOf<String>().also {
            it.add(Utils.defaultViewModel)
            it.addAll(setting.viewModelMap.keys)
        }
    }

    val layoutSet: HashSet<String> by lazy {
        hashSetOf<String>().also {
            it.add(Utils.defaultLayout)
            it.addAll(setting.layoutMap.keys)
        }
    }
}