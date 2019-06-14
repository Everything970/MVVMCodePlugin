package com.chenan.mvvm.action

import com.chenan.mvvm.ui.CreateBeanDialog
import com.chenan.mvvm.util.BeanCodeHelper
import com.chenan.mvvm.util.Utils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager

class CreateBeanCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)
        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        if (project == null || virtualFile == null) {
            Utils.showError("转化失败！\nproject:$project packageFile:$virtualFile")
            return
        }
        val packageName = virtualFile.path.let {
            val start = it.indexOf("/main/java") + 11
            it.substring(start, it.length).trim().replace('/', '.')
        }
        CreateBeanDialog().apply {
            title = "创建Bean Class"
            setListener(object : CreateBeanDialog.OnClickListener {

                override fun onOK(beanName: String, json: String) {
                    val helper = BeanCodeHelper()
                    PsiManager.getInstance(project).findDirectory(virtualFile)?.let { psiDirectory ->
                        val psiFile = psiDirectory.findFile("$beanName.kt")
                        if (psiFile == null) {
                            psiDirectory.createFile("$beanName.kt").let {
                                PsiDocumentManager.getInstance(project).getDocument(it)?.setText(helper.getBeanString(json, packageName, beanName))
                            }
                        } else {
                            DialogBuilder().apply {
                                setTitle("提示")
                                setErrorText("已存在该类，是否继续生成代码。")
                                setOkOperation {
                                    PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.getBeanString(json, packageName, beanName))
                                    dialogWrapper.close(0)
                                }
                            }.show()
                        }
                    }
                }

                override fun onCancel() {

                }
            })
        }.showDialog()
    }
}
