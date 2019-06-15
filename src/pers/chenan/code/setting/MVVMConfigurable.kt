package pers.chenan.code.setting

import pers.chenan.code.code.TemplateCode
import pers.chenan.code.ui.MVVMSettingUI
import pers.chenan.code.ui.WriteCodeDialog
import pers.chenan.code.util.Utils
import pers.chenan.code.util.pathByProject
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

    private val setting = MVVMSetting.getInstance(project)
    private val ui = MVVMSettingUI()
    private var selectedRetrofitPath: String = ""


    override fun getId(): String {
        return "setting.MVVMConfigurable"
    }

    override fun getDisplayName(): String {
        return "代码（MVVM）生成设置"
    }

    override fun reset() {
        ui.checkBoxConfrim.isSelected = setting.isNeedConfirm
        //activity Item
        ui.comboBoxActivity.removeAllItems()
        ui.comboBoxActivity.addItem(Utils.defaultActivity)
        setting.activityMap.keys.forEach {
            ui.comboBoxActivity.addItem(it)
        }
        if (!setting.containsActivity(setting.activity)) {
            setting.activity = Utils.defaultActivity
        }
        ui.comboBoxActivity.selectedItem = setting.activity
        //viewModel Item
        ui.comboBoxViewModel.removeAllItems()
        ui.comboBoxViewModel.addItem(Utils.defaultViewModel)
        setting.viewModelMap.keys.forEach {
            ui.comboBoxViewModel.addItem(it)
        }
        if (!setting.containsViewModel(setting.viewModel)) {
            setting.viewModel = Utils.defaultViewModel
        }
        ui.comboBoxViewModel.selectedItem = setting.viewModel
        //layout Item
        ui.comboBoxLayout.removeAllItems()
        ui.comboBoxLayout.addItem(Utils.defaultLayout)
        setting.layoutMap.keys.forEach {
            ui.comboBoxLayout.addItem(it)
        }
        if (!setting.containsLayout(setting.layout)) {
            setting.layout = Utils.defaultLayout
        }
        ui.comboBoxLayout.selectedItem = setting.layout

        ui.checkBoxRetrofit.isSelected = setting.isOpen
        ui.jPanelBean.isVisible = setting.isOpen
        ui.jPanelRetrofit.isVisible = setting.isOpen
        ui.textAreaFunCode.text = setting.interfaceFunCode
        ui.textFieldBeanPath.text = setting.beanPackagePath
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
        setting.isNeedConfirm = ui.checkBoxConfrim.isSelected
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
                    setting.beanPackagePath = ui.textFieldBeanPath.text
                    setting.retrofitPath = selectedRetrofitPath
                    setting.retrofitInterface = ui.comboBoxInterface.selectedItem.toString()
                    setting.interfaceFunCode = ui.textAreaFunCode.text
                }
            }
        } else {
            setting.beanPackagePath = ""
            setting.retrofitPath = ""
            setting.retrofitInterface = ""
        }
    }

    override fun isModified(): Boolean {
        return ui.checkBoxConfrim.isSelected != setting.isNeedConfirm
                || ui.comboBoxActivity.selectedItem != setting.activity || ui.comboBoxViewModel.selectedItem != setting.viewModel || ui.comboBoxLayout.selectedItem != setting.layout
                || ui.checkBoxRetrofit.isSelected != setting.isOpen || ui.textFieldBeanPath.text != setting.beanPackagePath || selectedRetrofitPath != setting.retrofitPath
                || ui.comboBoxInterface.selectedItem != setting.retrofitInterface || ui.textAreaFunCode.text != setting.interfaceFunCode
    }

    override fun createComponent(): JComponent? {
        ui.checkBoxConfrim.isSelected = true
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
            setting.editTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btAddActivity.addActionListener {
            setting.addTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btDeleteActivity.addActionListener {
            setting.deleteTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btViewModel.addActionListener {
            setting.editTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btAddViewModel.addActionListener {
            setting.addTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btDeleteViewModel.addActionListener {
            setting.deleteTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btLayout.addActionListener {
            setting.editTemplateCode(ui.comboBoxLayout, 2)
        }
        ui.btAddLayout.addActionListener {
            setting.addTemplateCode(ui.comboBoxLayout, 2)
        }
        ui.btDeleteLayout.addActionListener {
            setting.deleteTemplateCode(ui.comboBoxLayout, 2)
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