package com.chenan.mvvm.action

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.setting.MVVMSetting
import com.chenan.mvvm.util.Utils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class CreateViewCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (!e.presentation.isEnabled) {
            Utils.showError("请确认是否处于编辑Activity（kotlin）文件下")
        }
        val project = e.getData(PlatformDataKeys.PROJECT)!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)!!
        val setting = MVVMSetting.getInstance(project)
        if (setting.activity.isEmpty() || setting.viewModel.isEmpty() || setting.layout.isEmpty()) {
            Utils.showError("请先配置activity/viewModel/layout")
            return
        }

        val activityName = psiFile.name.replace("Activity.kt", "")
        val parentPath = psiFile.virtualFile.parent.path
        val index = parentPath.lastIndexOf("/java")
        val aIndex = parentPath.lastIndexOf("/activity")
        val hadActivity = aIndex > 0
        val packageName = (if (hadActivity) {
            parentPath.substring(index + 6, aIndex)
        } else {
            parentPath.substring(index + 6, parentPath.length)
        }).replace('/', '.')


        //create code of activity
        WriteCommandAction.runWriteCommandAction(project) {
            setting.activityCode?.let {
                editor.document.setText(Utils.getCodeContent(it, packageName, activityName))
            } ?: Utils.showError("所选activity模板为空")

        }
        //create code and file of view model
        (if (hadActivity) psiFile.parent?.parent else psiFile.parent)?.let {
            val viewModel = it.findSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
                    ?: it.createSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
            val model = viewModel.findFile(activityName + "ViewModel.kt")
                    ?: viewModel.createFile(activityName + "ViewModel.kt")
            setting.viewModelCode?.let { code ->
                Utils.getCodeContent(code, packageName, activityName).let { content ->
                    PsiDocumentManager.getInstance(project).getDocument(model)?.setText(content)
                }
            } ?: Utils.showError("所选ViewModel模板为空")
        }

        //change xml
        val xml = FilenameIndex.getFilesByName(project, "activity${Utils.getLowerActivityName(activityName)}.xml", GlobalSearchScope.allScope(project))
        if (xml.isNotEmpty()) {
            val xmlEditor = PsiDocumentManager.getInstance(project).getDocument(xml[0])
            setting.layoutCode?.let {
                xmlEditor?.setText(Utils.getCodeContent(it, packageName, activityName))
            } ?: Utils.showError("所选Layout模板为空")
        }
    }


    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)
        e.presentation.isEnabled = psiFile?.name.let { it != null && it.endsWith("Activity.kt") }
    }

}
