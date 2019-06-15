package pers.chenan.code.action

import pers.chenan.code.ui.CreateBeanDialog
import pers.chenan.code.util.BeanCodeHelper
import pers.chenan.code.util.Utils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction

class MakeBeanCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (!e.presentation.isEnabled) {
            Utils.showError("请确认是否处于编辑kotlin文件下")
        }
        val project = e.getData(PlatformDataKeys.PROJECT)
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)
        val editor = e.getData(PlatformDataKeys.EDITOR)
        if (project == null || editor == null || psiFile == null) {
            Utils.showError("转化失败！\nproject:$project editor:$editor psiFile:$psiFile")
            return
        }
        val name = psiFile.name.replace(".kt", "")
        val packageName = editor.document.text.let {
            val start = it.indexOf("package") + 7
            val end = it.indexOf('\n', start)
            it.substring(start, end).trim()
        }
        CreateBeanDialog().apply {
            title = "生成 Bean Class 代码"
            setListener(object : CreateBeanDialog.OnClickListener {

                override fun onOK(beanName: String, json: String) {
                    val helper = BeanCodeHelper()
                    if (beanName != name) {
                        psiFile.name = "$beanName.kt"
                    }
                    WriteCommandAction.runWriteCommandAction(project) {
                        editor.document.setText(helper.getBeanString(json, packageName, beanName))
                    }
                }

                override fun onCancel() {

                }
            })
        }.showDialog(name)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)
        e.presentation.isEnabled = psiFile?.name.let { it != null && it.endsWith(".kt") }
    }
}
