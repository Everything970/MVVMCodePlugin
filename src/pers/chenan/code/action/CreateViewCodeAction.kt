package pers.chenan.code.action

import pers.chenan.code.code.TemplateCode
import pers.chenan.code.setting.MVVMSetting
import pers.chenan.code.util.Utils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import pers.chenan.code.setting.PluginSetting
import pers.chenan.code.ui.CreateCodeDialog

class CreateViewCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (!e.presentation.isEnabled) {
            Utils.showError("请确认是否处于编辑Activity（kotlin）文件下")
        }
        val project = e.getData(PlatformDataKeys.PROJECT)!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)!!
        val setting = PluginSetting.getInstance(project)
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

        if (!setting.mvvm.isNeedConfirm) {
            createCode(project, setting, psiFile, editor, hadActivity, packageName, activityName)
        } else {
            CreateCodeDialog(project).apply {
                setListener(object : CreateCodeDialog.OnClickListener {
                    override fun onOk(activity: String, viewModel: String, layout: String) {
                        setting.mvvm.activity = activity
                        setting.mvvm.viewModel = viewModel
                        setting.mvvm.layout = layout
                        createCode(project, setting, psiFile, editor, hadActivity, packageName, activityName)
                    }

                    override fun onCancel() {
                    }
                })
            }.showDialog()
        }
    }

    private fun createCode(project: Project, setting: PluginSetting, psiFile: PsiFile, editor: Editor, hadActivity: Boolean, packageName: String, activityName: String) {
        //create code of activity
        WriteCommandAction.runWriteCommandAction(project) {
            setting.mvvm.activityCode?.let {
                editor.document.setText(Utils.getCodeContent(it, packageName, activityName))
            } ?: Utils.showError("所选activity模板为空")

        }
        //create code and file of view model
        (if (hadActivity) psiFile.parent?.parent else psiFile.parent)?.let {
            val viewModel = it.findSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
                    ?: it.createSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
            val model = viewModel.findFile(activityName + "ViewModel.kt")
                    ?: viewModel.createFile(activityName + "ViewModel.kt")
            setting.mvvm.viewModelCode?.let { code ->
                Utils.getCodeContent(code, packageName, activityName).let { content ->
                    PsiDocumentManager.getInstance(project).getDocument(model)?.setText(content)
                }
            } ?: Utils.showError("所选ViewModel模板为空")
        }

        //change xml
        val xml = FilenameIndex.getFilesByName(project, "activity${Utils.getLowerActivityName(activityName)}.xml", GlobalSearchScope.allScope(project))
        if (xml.isNotEmpty()) {
            val xmlEditor = PsiDocumentManager.getInstance(project).getDocument(xml[0])
            setting.mvvm.layoutCode?.let {
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
