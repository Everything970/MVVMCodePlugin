package pers.chenan.code.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import pers.chenan.code.ui.CreateBeanDialog
import pers.chenan.code.util.BeanCodeHelper
import pers.chenan.code.util.Utils

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

                override fun onOK(beanName: String, json: String,al:Int) {
                    val helper = BeanCodeHelper()
                    PsiManager.getInstance(project).findDirectory(virtualFile)?.let { psiDirectory ->
                        val psiFile = psiDirectory.findFile("$beanName.kt")
                        if (psiFile == null) {
                            psiDirectory.createFile("$beanName.kt").let {
                                PsiDocumentManager.getInstance(project).getDocument(it)?.setText(helper.getBeanString(json, packageName, beanName,al))
                            }
                        } else {
                            DialogBuilder().apply {
                                setTitle("提示")
                                setErrorText("已存在该类，是否继续生成代码。")
                                setOkOperation {
                                    PsiDocumentManager.getInstance(project).getDocument(psiFile)?.setText(helper.getBeanString(json, packageName, beanName,al))
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
