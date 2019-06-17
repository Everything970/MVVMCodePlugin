package pers.chenan.code.setting

import pers.chenan.code.code.TemplateCode
import pers.chenan.code.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import org.jdom.Element
import pers.chenan.code.ui.WriteCodeDialog
import javax.swing.JComboBox

class PluginSetting : PersistentStateComponent<Element> {

    val mvvm = MVVMSetting.getInstance()
    var isOpen: Boolean = false
    var beanPackagePath: String = ""
    var retrofitPath: String = ""
    var retrofitInterface: String = ""
    var interfaceFunCode: String = TemplateCode.interfaceFunCode


    override fun getState(): Element? {
        return Element("PluginSetting").apply {
            setAttribute("is_open", isOpen.toString())
            setAttribute("bean_path", beanPackagePath)
            setAttribute("retrofit_path", retrofitPath)
            setAttribute("retrofit_interface", retrofitInterface)
            setAttribute("interface_fun_code", interfaceFunCode)
        }
    }

    override fun loadState(p0: Element) {
        isOpen = p0.getAttributeValue("is_open")?.toBoolean() ?: false
        beanPackagePath = p0.getAttributeValue("bean_path") ?: ""
        retrofitPath = p0.getAttributeValue("retrofit_path") ?: ""
        retrofitInterface = p0.getAttributeValue("retrofit_interface") ?: ""
        interfaceFunCode = p0.getAttributeValue("interface_fun_code") ?: TemplateCode.interfaceFunCode

    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PluginSetting {
            return ServiceManager.getService(project, PluginSetting::class.java)
        }
    }
}