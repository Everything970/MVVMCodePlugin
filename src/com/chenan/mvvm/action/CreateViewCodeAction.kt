package com.chenan.mvvm.action

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.ui.CreateCodeDialog
import com.chenan.mvvm.ui.WriteCodeDialog
import com.chenan.mvvm.util.Utils
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class CreateViewCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (!e.presentation.isEnabled){
            Utils.showError("请确认是否处于编辑Activity（kotlin）文件下")
        }
        val project = e.getData(PlatformDataKeys.PROJECT)
        val editor = e.getData(PlatformDataKeys.EDITOR)
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)
        if (project == null || editor == null || psiFile == null) {
            Utils.showError("转化失败！\nproject:$project editor:$editor psiFile:$psiFile")
            return
        }
        CreateCodeDialog().apply {
            setListener(object : CreateCodeDialog.OnClickListener {
                override fun onAddActivity() {
                    WriteCodeDialog().also {
                        it.title = "Activity Code"
                        it.setListener(object : WriteCodeDialog.OnClickListener {
                            override fun onOk(name: String, content: String) {
                                val txtName = if (name.endsWith(".txt")) name else "$name.txt"
                                val file = Utils.createCode(Utils.getPluginPath(), TemplateCode.TYPE_ACTIVITY, txtName, content)
                                if (file == null) {
                                    Utils.showError("创建 $txtName.txt 失败")
                                } else {
                                    addActivity(file)
                                }
                            }

                            override fun onCancel() {

                            }
                        })
                    }.showDialog()
                }

                override fun onAddViewModel() {
                    WriteCodeDialog().also {
                        it.title = "ViewModel Code"
                        it.setListener(object : WriteCodeDialog.OnClickListener {
                            override fun onOk(name: String, content: String) {
                                val txtName = if (name.endsWith(".txt")) name else "$name.txt"
                                val file = Utils.createCode(Utils.getPluginPath(), TemplateCode.TYPE_VIEW_MODEL, txtName, content)
                                if (file == null) {
                                    Utils.showError("创建 $txtName.txt 失败")
                                } else {
                                    addViewModel(file)
                                }
                            }

                            override fun onCancel() {

                            }
                        })
                    }.showDialog()
                }

                override fun onAddLayout() {
                    WriteCodeDialog().also {
                        it.title = "Layout Code"
                        it.setListener(object : WriteCodeDialog.OnClickListener {
                            override fun onOk(name: String, content: String) {
                                val txtName = if (name.endsWith(".txt")) name else "$name.txt"
                                val file = Utils.createCode(Utils.getPluginPath(), TemplateCode.TYPE_LAYOUT, txtName, content)
                                if (file == null) {
                                    Utils.showError("创建 $txtName.txt 失败")
                                } else {
                                    addLayout(file)
                                }
                            }

                            override fun onCancel() {

                            }
                        })
                    }.showDialog()
                }

                override fun onOk(activityFile: File, viewModelFile: File, layoutFile: File) {
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
                        editor.document.setText(Utils.getCodeContent(Utils.getCode("${TemplateCode.TYPE_ACTIVITY}/${activityFile.name}"), packageName, activityName))
                    }
                    //create code and file of view model
                    (if (hadActivity) psiFile.parent?.parent else psiFile.parent)?.let {
                        val viewModel = it.findSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
                                ?: it.createSubdirectory(TemplateCode.TYPE_VIEW_MODEL)
                        val model = viewModel.findFile(activityName + "ViewModel.kt")
                                ?: viewModel.createFile(activityName + "ViewModel.kt")
                        val content = Utils.getCodeContent(Utils.getCode("${TemplateCode.TYPE_VIEW_MODEL}/${viewModelFile.name}"), packageName, activityName)
                        PsiDocumentManager.getInstance(project).getDocument(model)?.setText(content)
                    }

                    //change xml
                    val xml = FilenameIndex.getFilesByName(project, "activity${Utils.getLowerActivityName(activityName)}.xml", GlobalSearchScope.allScope(project))
                    if (xml.isNotEmpty()) {
                        val xmlEditor = PsiDocumentManager.getInstance(project).getDocument(xml[0])
                        xmlEditor?.setText(Utils.getCodeContent(Utils.getCode(TemplateCode.TYPE_LAYOUT+ "/${layoutFile.name}"), packageName, activityName))
                    }
                }

                override fun onCancel() {

                }
            })
        }.showDialog()

    }


    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(PlatformDataKeys.PSI_FILE)
        e.presentation.isEnabled = psiFile?.name.let { it != null && it.endsWith("Activity.kt") }
    }

}
