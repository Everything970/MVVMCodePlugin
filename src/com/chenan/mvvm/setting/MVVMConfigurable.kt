package com.chenan.mvvm.setting

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.ui.MVVMSettingUI
import com.chenan.mvvm.ui.WriteCodeDialog
import com.chenan.mvvm.util.PluginHelper
import com.chenan.mvvm.util.Utils
import com.chenan.mvvm.util.pathByProject
import com.intellij.ide.util.PackageChooserDialog
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import java.util.regex.Pattern
import javax.swing.JCheckBox
import javax.swing.JComponent

class MVVMConfigurable(private val project: Project) : SearchableConfigurable {

    private val helper = PluginHelper.getInstance(project)
    private val setting = helper.setting
    private val ui = MVVMSettingUI()
    private var selectedRetrofitPath: String = ""


    override fun getId(): String {
        return "setting.MVVMConfigurable"
    }

    override fun getDisplayName(): String {
        return "代码（MVVM）生成设置"
    }

    override fun reset() {
        helper.activitySet.forEach {
            ui.comboBoxActivity.addItem(it)
        }
        helper.viewModelSet.forEach { ui.comboBoxViewModel.addItem(it) }
        helper.layoutSet.forEach { ui.comboBoxLayout.addItem(it) }
        helper.activitySet.indexOfFirst { it == setting.activity }.let {
            if (it >= 0) {
                ui.comboBoxActivity.selectedIndex = it
            } else {
                setting.activity = ui.comboBoxActivity.selectedItem.toString()
            }
        }
        helper.viewModelSet.indexOfFirst { it == setting.viewModel }.let {
            if (it >= 0) {
                ui.comboBoxViewModel.selectedIndex = it
            } else {
                setting.viewModel = ui.comboBoxViewModel.selectedItem.toString()
            }
        }
        helper.layoutSet.indexOfFirst { it == setting.layout }.let {
            if (it >= 0) {
                ui.comboBoxLayout.selectedIndex = it
            } else {
                setting.layout = ui.comboBoxLayout.selectedItem.toString()
            }
        }
        ui.checkBoxRetrofit.isSelected = setting.isOpen
        ui.jPanelBean.isVisible = setting.isOpen
        ui.jPanelRetrofit.isVisible = setting.isOpen
        ui.textAreaFunCode.text = setting.interfaceFunCode
        ui.textFieldBeanPath.text = setting.beanPath
        if (setting.retrofitPath.isNotEmpty())
            VirtualFileManager.getInstance().findFileByUrl(setting.retrofitPath)?.let { virtualFile ->
                PsiManager.getInstance(project).findFile(virtualFile)?.let { psiFile ->
                    ui.textFieldRetrofitPath.text = psiFile.pathByProject
                    PsiDocumentManager.getInstance(project).getDocument(psiFile)?.let { document ->
                        val p = Pattern.compile("(interface\\s\\w*)")
                        p.matcher(document.text)?.let { matcher ->
                            while (matcher.find()) {
                                ui.comboBoxInterface.addItem(matcher.group(1))
                            }
                        }
                        if (setting.retrofitInterface.isNotEmpty())
                            ui.comboBoxInterface.selectedItem = setting.retrofitInterface
                    }
                }
            }

    }

    @Throws(NullPointerException::class)
    override fun apply() {
        setting.activity = ui.comboBoxActivity.selectedItem.toString()
        setting.viewModel = ui.comboBoxViewModel.selectedItem.toString()
        setting.layout = ui.comboBoxLayout.selectedItem.toString()
        setting.isOpen = ui.checkBoxRetrofit.isSelected
        if (ui.checkBoxRetrofit.isSelected) {
            when {
                ui.textFieldBeanPath.text.isNullOrEmpty() -> {
                    Utils.showError("Bean类路径不能为空")
                    throw NullPointerException("Bean类路径不能为空")
                }
                selectedRetrofitPath.isNullOrEmpty() -> {
                    Utils.showError("Retrofit路径不能为空")
                    throw NullPointerException("Retrofit路径不能为空")
                }
                ui.comboBoxInterface.selectedItem == null -> {
                    Utils.showError("Retrofit interface不能为空")
                    throw NullPointerException("Retrofit interface类路径不能为空")
                }
                ui.textAreaFunCode.text.isNullOrEmpty() -> {
                    Utils.showError("方法模板不能为空")
                    throw NullPointerException("方法模板不能为空")
                }
                else -> {
                    setting.beanPath = ui.textFieldBeanPath.text
                    setting.retrofitPath = selectedRetrofitPath
                    setting.retrofitInterface = ui.comboBoxInterface.selectedItem.toString()
                    setting.interfaceFunCode = ui.textAreaFunCode.text
                }
            }
        } else {
            setting.beanPath = ""
            setting.retrofitPath = ""
            setting.retrofitInterface = ""
        }
    }

    override fun isModified(): Boolean {
        return ui.comboBoxActivity.selectedItem != setting.activity || ui.comboBoxViewModel.selectedItem != setting.viewModel || ui.comboBoxLayout.selectedItem != setting.layout
                || ui.checkBoxRetrofit.isSelected != setting.isOpen || ui.textFieldBeanPath.text != setting.beanPath || selectedRetrofitPath != setting.retrofitPath
                || ui.comboBoxInterface.selectedItem != setting.retrofitInterface || ui.textAreaFunCode.text != setting.interfaceFunCode
    }

    override fun createComponent(): JComponent? {
        ui.checkBoxRetrofit.isSelected = false
        ui.jPanelBean.isVisible = false
        ui.jPanelRetrofit.isVisible = false
        ui.textFieldRetrofitPath.isEditable = false
        ui.textFieldBeanPath.isEditable = false
        ui.panelFunCode.isVisible = false
        setEvent()
        return ui.contentPanel
    }

    private fun setEvent() {
        ui.checkBoxRetrofit.addActionListener { event ->
            (event.source as? JCheckBox)?.also {
                ui.jPanelBean.isVisible = it.isSelected
                ui.jPanelRetrofit.isVisible = it.isSelected
                ui.panelFunCode.isVisible = it.isSelected
            }
        }
        ui.btActivity.addActionListener {
            val item = ui.comboBoxActivity.selectedItem
            if (Utils.defaultActivity == item) {
                Utils.showError("默认模板不可编辑")
                return@addActionListener
            }
            WriteCodeDialog().also {
                it.title = "编辑 Activity"
                it.setListener { name, content ->
                    if (name == item) {
                        setting.activityMap[name] = content
                        it.dispose()
                    } else {
                        if (helper.activitySet.contains(name)) {
                            Utils.showError("已存在相同命名模板")
                        } else {
                            setting.activityMap[name] = content
                            setting.activityMap.remove(item.toString())
                            helper.activitySet.add(name)
                            helper.activitySet.remove(item.toString())
                            ui.comboBoxActivity.addItem(name)
                            ui.comboBoxActivity.selectedItem = name
                            ui.comboBoxActivity.removeItem(item)
                            it.dispose()
                        }
                    }
                }
            }.showDialog(item.toString(), setting.activityMap[item.toString()])
        }
        ui.btAddActivity.addActionListener {
            WriteCodeDialog().also {
                it.title = "Activity Code"
                it.setListener { name, content ->
                    if (helper.activitySet.contains(name)) {
                        Utils.showError("已存在相同命名模板")
                    } else {
                        setting.activityMap[name] = content
                        helper.activitySet.add(name)
                        ui.comboBoxActivity.addItem(name)
                        ui.comboBoxActivity.selectedItem = name
                        it.dispose()
                    }
                }
            }.showDialog("", TemplateCode.activityCode)
        }
        ui.btDeleteActivity.addActionListener {
            val item = ui.comboBoxActivity.selectedItem
            if (Utils.defaultActivity == item) {
                Utils.showError("默认模板不可删除")
                return@addActionListener
            }
            JBPopupFactory.getInstance().createConfirmation("确认删除${item}吗？", "确定", "取消", {
                helper.activitySet.remove(item.toString())
                setting.activityMap.remove(item.toString())
                ui.comboBoxActivity.removeItem(item)
            }, 0).showInFocusCenter()
        }
        ui.btViewModel.addActionListener {
            val item = ui.comboBoxViewModel.selectedItem
            if (Utils.defaultViewModel == item) {
                Utils.showError("默认模板不可编辑")
                return@addActionListener
            }
            WriteCodeDialog().also {
                it.title = "编辑 ViewModel"
                it.setListener { name, content ->
                    if (name == item) {
                        setting.viewModelMap[name] = content
                        it.dispose()
                    } else {
                        if (helper.viewModelSet.contains(name)) {
                            Utils.showError("已存在相同命名模板")
                        } else {
                            setting.viewModelMap[name] = content
                            setting.viewModelMap.remove(item.toString())
                            helper.viewModelSet.add(name)
                            helper.viewModelSet.remove(item.toString())
                            ui.comboBoxViewModel.addItem(name)
                            ui.comboBoxViewModel.selectedItem = name
                            ui.comboBoxViewModel.removeItem(item)
                            it.dispose()
                        }
                    }
                }
            }.showDialog(item.toString(), setting.viewModelMap[item.toString()])
        }
        ui.btAddViewModel.addActionListener {
            WriteCodeDialog().also {
                it.title = "ViewModel Code"
                it.setListener { name, content ->
                    if (helper.viewModelSet.contains(name)) {
                        Utils.showError("已存在相同命名模板")
                    } else {
                        setting.viewModelMap[name] = content
                        helper.viewModelSet.add(name)
                        ui.comboBoxViewModel.addItem(name)
                        ui.comboBoxViewModel.selectedItem = name
                        it.dispose()
                    }
                }
            }.showDialog("", TemplateCode.viewModelCode)
        }
        ui.btDeleteViewModel.addActionListener {
            val item = ui.comboBoxViewModel.selectedItem
            if (Utils.defaultViewModel == item) {
                Utils.showError("默认模板不可删除")
                return@addActionListener
            }
            JBPopupFactory.getInstance().createConfirmation("确认删除${item}吗？", "确定", "取消", {
                helper.viewModelSet.remove(item.toString())
                setting.viewModelMap.remove(item.toString())
                ui.comboBoxViewModel.removeItem(item)
            }, 0).showInFocusCenter()
        }
        ui.btLayout.addActionListener {
            val item = ui.comboBoxLayout.selectedItem
            if (Utils.defaultLayout == item) {
                Utils.showError("默认模板不可编辑")
                return@addActionListener
            }
            WriteCodeDialog().also {
                it.title = "编辑 Layout"
                it.setListener { name, content ->
                    if (name == item) {
                        setting.layoutMap[name] = content
                        it.dispose()
                    } else {
                        if (helper.layoutSet.contains(name)) {
                            Utils.showError("已存在相同命名模板")
                        } else {
                            setting.layoutMap[name] = content
                            setting.layoutMap.remove(item.toString())
                            helper.layoutSet.add(name)
                            helper.layoutSet.remove(item.toString())
                            ui.comboBoxLayout.addItem(name)
                            ui.comboBoxLayout.selectedItem = name
                            ui.comboBoxLayout.removeItem(item)
                            it.dispose()
                        }
                    }
                }
            }.showDialog(item.toString(), setting.layoutMap[item.toString()])
        }
        ui.btAddLayout.addActionListener {
            WriteCodeDialog().also {
                it.title = "Layout Code"
                it.setListener { name, content ->
                    if (helper.activitySet.contains(name)) {
                        Utils.showError("已存在相同命名模板")
                    } else {
                        setting.layoutMap[name] = content
                        helper.layoutSet.add(name)
                        ui.comboBoxLayout.addItem(name)
                        ui.comboBoxLayout.selectedItem = name
                        it.dispose()
                    }
                }
            }.showDialog("", TemplateCode.layoutCode)
        }
        ui.btDeleteLayout.addActionListener {
            val item = ui.comboBoxLayout.selectedItem
            if (Utils.defaultLayout == item) {
                Utils.showError("默认模板不可删除")
                return@addActionListener
            }
            JBPopupFactory.getInstance().createConfirmation("确认删除${item}吗？", "确定", "取消", {
                helper.layoutSet.remove(item.toString())
                setting.layoutMap.remove(item.toString())
                ui.comboBoxLayout.removeItem(item)
            }, 0).showInFocusCenter()
        }
        ui.btSelectBeanPath.addActionListener {
            val chooser = PackageChooserDialog("选择新增Bean类所在包", project)
            chooser.selectPackage(ui.textFieldBeanPath.text)
            if (chooser.showAndGet()) {
                val mPackage = chooser.selectedPackage
                if (mPackage != null) {
                    ui.textFieldBeanPath.text = mPackage.qualifiedName
                }
            }
        }
        ui.btSelectRetrofit.addActionListener {
            val chooser = TreeClassChooserFactory.getInstance(project).createFileChooser("选择Retrofit接口类所在包", null, null, {
                it.name.endsWith(".kt")
            }, true, false)
            chooser.showDialog()
            chooser.selectedFile?.let { psiFile ->
                println(psiFile.name)
                ui.textFieldRetrofitPath.text = psiFile.pathByProject
                selectedRetrofitPath = psiFile.virtualFile.url
                ui.comboBoxInterface.removeAll()
                PsiDocumentManager.getInstance(project).getDocument(psiFile)?.let { document ->
                    val p = Pattern.compile("(interface\\s\\w*)")
                    p.matcher(document.text)?.let { matcher ->
                        while (matcher.find()) {
                            ui.comboBoxInterface.addItem(matcher.group(1))
                        }
                    }
                }
            }
        }
    }

}