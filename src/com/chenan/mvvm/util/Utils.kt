package com.chenan.mvvm.util

import com.chenan.mvvm.code.TemplateCode
import com.intellij.openapi.ui.DialogBuilder

object Utils {
    const val defaultActivity = "activity_code"
    const val defaultViewModel = "view_model_code"
    const val defaultLayout = "layout_code"

    fun getLowerActivityName(activityName: String): String {
        val lowerName = StringBuffer()
        activityName.forEach {
            if (it.isLowerCase()) {
                lowerName.append(it)
            } else {
                lowerName.append('_').append(it.toLowerCase())
            }
        }
        return lowerName.toString()
    }

    fun getCodeContent(templateCode: String, packageName: String, activityName: String): String {
        return templateCode.replace(TemplateCode.packageName, packageName)
                .replace(TemplateCode.activityName, activityName)
                .replace(TemplateCode.lowerActivityName, getLowerActivityName(activityName))
    }

    @JvmStatic
    fun showError(error: String) {
        DialogBuilder().apply {
            setTitle("提示")
            setErrorText(error)
            addOkAction()
        }.show()
    }
}