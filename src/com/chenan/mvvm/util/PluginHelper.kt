package com.chenan.mvvm.util

import com.intellij.openapi.module.ModuleManager
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

    val activityList = Utils.getActivityFiles()
    val viewModelList = Utils.getViewModelFiles()
    val layoutList = Utils.getLayoutFiles()

    val projectFilePath=project.projectFilePath
    val modules=ModuleManager.getInstance(project).modules
}