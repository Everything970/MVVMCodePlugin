package com.chenan.mvvm.action

import com.chenan.mvvm.setting.MVVMSetting
import com.chenan.mvvm.ui.CreateURLDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreateURLAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        CreateURLDialog(e.project).showDialog()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
//        e.project.let { project ->
//            if (project == null) {
//                e.presentation.isEnabled = false
//            } else {
//                MVVMSetting.getInstance(project).let {
//                    e.presentation.isEnabled = it.isOpen
//                }
//            }
//        }

    }
}
