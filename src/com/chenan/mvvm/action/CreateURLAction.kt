package com.chenan.mvvm.action

import com.chenan.mvvm.setting.MVVMSetting
import com.chenan.mvvm.ui.CreateURLDialog
import com.chenan.mvvm.util.FunCodeHelper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager

class CreateURLAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        CreateURLDialog(project).apply {
            setOnClickListener(object : CreateURLDialog.OnClickListener {
                override fun onOK(helper: FunCodeHelper) {
                    createCode(helper, project, editor)
                }

                override fun onCancel() {

                }
            })
        }.showDialog()
    }

    private fun createCode(helper: FunCodeHelper, project: Project, editor: Editor) {
        val setting = MVVMSetting.getInstance(project)
        VirtualFileManager.getInstance().findFileByUrl(setting.retrofitPath)?.let { virtualFile ->
            PsiManager.getInstance(project).findFile(virtualFile)?.let { psiFile ->
                PsiDocumentManager.getInstance(project).getDocument(psiFile)?.let { document ->
                    var start = document.text.indexOf(setting.retrofitInterface)
                    start = document.text.indexOf('{', start)
                    if (start >= 0) {
                        start += 2
                        WriteCommandAction.runWriteCommandAction(project) {
                            document.insertString(start, helper.funCode.replace("\n", "\n\t\t"))
                        }
                    }
                }
            }
        }
        if (helper.requestType == "Bean") {
            JavaPsiFacade.getInstance(project).findPackage(setting.beanPackagePath)?.directories?.get(0)?.let { psiDirectory ->
                psiDirectory.findFile(helper.requestName + ".kt")?.let { psiFile ->
                    DialogBuilder().apply {
                        setTitle("提示")
                        setErrorText("已存在${helper.requestName}类，是否替换代码。")
                        setOkOperation {
                            PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.requestContent)
                            dialogWrapper.close(0)
                        }
                    }.show()
                } ?: psiDirectory.createFile(helper.requestName + ".kt").let { psiFile ->
                    PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.requestContent)
                }
            }
        }
        if (helper.resultType == "Bean") {
            JavaPsiFacade.getInstance(project).findPackage(setting.beanPackagePath)?.directories?.get(0)?.let { psiDirectory ->
                psiDirectory.findFile(helper.resultName + ".kt")?.let { psiFile ->
                    DialogBuilder().apply {
                        setTitle("提示")
                        setErrorText("已存在${helper.resultName}类，是否替换代码。")
                        setOkOperation {
                            PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.resultContent)
                            dialogWrapper.close(0)
                        }
                    }.show()
                } ?: psiDirectory.createFile(helper.resultName + ".kt").let { psiFile ->
                    PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.resultContent)
                }
            } ?: println("package null")
        }
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(editor.caretModel.offset, ".${helper.funName}()")
        }
        editor.caretModel.moveToOffset(editor.caretModel.offset + helper.funName.length + if (helper.requestType != "Null") 2 else 3)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.project.let { project ->
            if (project == null) {
                e.presentation.isEnabled = false
            } else {
                MVVMSetting.getInstance(project).let {
                    e.presentation.isEnabled = it.isOpen
                }
            }
        }

    }
}
