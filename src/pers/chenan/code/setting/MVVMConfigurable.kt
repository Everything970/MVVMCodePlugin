package pers.chenan.code.setting

import pers.chenan.code.ui.MVVMSettingUI
import pers.chenan.code.util.Utils
import pers.chenan.code.util.pathByProject
import com.intellij.ide.util.PackageChooserDialog
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import java.util.regex.Pattern
import javax.swing.JCheckBox
import javax.swing.JComponent

class MVVMConfigurable(private val project: Project) : SearchableConfigurable {

    private val setting = PluginSetting.getInstance(project)
    private val ui = MVVMSettingUI()
    private var selectedRetrofitPath: String = ""


    override fun getId(): String {
        return "setting.MVVMConfigurable"
    }

    override fun getDisplayName(): String {
        return "代码（MVVM）生成设置"
    }

    override fun reset() {
        ui.checkBoxConfrim.isSelected = setting.mvvm.isNeedConfirm
        //activity Item
        ui.comboBoxActivity.removeAllItems()
        ui.comboBoxActivity.addItem(Utils.defaultActivity)
        setting.mvvm.activityMap.keys.forEach {
            ui.comboBoxActivity.addItem(it)
        }
        if (!setting.mvvm.containsActivity(setting.mvvm.activity)) {
            setting.mvvm.activity = Utils.defaultActivity
        }
        ui.comboBoxActivity.selectedItem = setting.mvvm.activity
        //viewModel Item
        ui.comboBoxViewModel.removeAllItems()
        ui.comboBoxViewModel.addItem(Utils.defaultViewModel)
        setting.mvvm.viewModelMap.keys.forEach {
            ui.comboBoxViewModel.addItem(it)
        }
        if (!setting.mvvm.containsViewModel(setting.mvvm.viewModel)) {
            setting.mvvm.viewModel = Utils.defaultViewModel
        }
        ui.comboBoxViewModel.selectedItem = setting.mvvm.viewModel
        //layout Item
        ui.comboBoxLayout.removeAllItems()
        ui.comboBoxLayout.addItem(Utils.defaultLayout)
        setting.mvvm.layoutMap.keys.forEach {
            ui.comboBoxLayout.addItem(it)
        }
        if (!setting.mvvm.containsLayout(setting.mvvm.layout)) {
            setting.mvvm.layout = Utils.defaultLayout
        }
        ui.comboBoxLayout.selectedItem = setting.mvvm.layout

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
        setting.mvvm.isNeedConfirm = ui.checkBoxConfrim.isSelected
        setting.mvvm.activity = ui.comboBoxActivity.selectedItem.toString()
        setting.mvvm.viewModel = ui.comboBoxViewModel.selectedItem.toString()
        setting.mvvm.layout = ui.comboBoxLayout.selectedItem.toString()
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
        return ui.checkBoxConfrim.isSelected != setting.mvvm.isNeedConfirm
                || ui.comboBoxActivity.selectedItem != setting.mvvm.activity || ui.comboBoxViewModel.selectedItem != setting.mvvm.viewModel || ui.comboBoxLayout.selectedItem != setting.mvvm.layout
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
            setting.mvvm.editTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btAddActivity.addActionListener {
            setting.mvvm.addTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btDeleteActivity.addActionListener {
            setting.mvvm.deleteTemplateCode(ui.comboBoxActivity, 0)
        }
        ui.btViewModel.addActionListener {
            setting.mvvm.editTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btAddViewModel.addActionListener {
            setting.mvvm.addTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btDeleteViewModel.addActionListener {
            setting.mvvm.deleteTemplateCode(ui.comboBoxViewModel, 1)
        }
        ui.btLayout.addActionListener {
            setting.mvvm.editTemplateCode(ui.comboBoxLayout, 2)
        }
        ui.btAddLayout.addActionListener {
            setting.mvvm.addTemplateCode(ui.comboBoxLayout, 2)
        }
        ui.btDeleteLayout.addActionListener {
            setting.mvvm.deleteTemplateCode(ui.comboBoxLayout, 2)
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