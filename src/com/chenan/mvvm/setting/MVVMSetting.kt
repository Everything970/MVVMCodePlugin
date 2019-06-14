package com.chenan.mvvm.setting

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.util.Utils
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import org.jdom.Element
import org.jf.util.SparseArray

class MVVMSetting : PersistentStateComponent<Element> {

    var activity: String = Utils.defaultActivity
    var viewModel: String = Utils.defaultViewModel
    var layout: String = Utils.defaultLayout
    var activity:

    var isOpen: Boolean = false
    var beanPath: String = ""
    var retrofitPath: String = ""
    var retrofitInterface: String = ""
    var interfaceFunCode: String = TemplateCode.interfaceFunCode


    override fun getState(): Element? {
        return Element("MVVMSetting").apply {
            setAttribute("activity", activity)
            setAttribute("view_model", viewModel)
            setAttribute("layout", layout)
            setAttribute("is_open", isOpen.toString())
            setAttribute("bean_path", beanPath)
            setAttribute("retrofit_path", retrofitPath)
            setAttribute("retrofit_interface", retrofitInterface)
        }
    }

    override fun loadState(p0: Element) {
        activity = p0.getAttributeValue("activity") ?: Utils.defaultActivity
        viewModel = p0.getAttributeValue("view_model") ?: Utils.defaultViewModel
        layout = p0.getAttributeValue("layout") ?: Utils.defaultLayout
        isOpen = p0.getAttributeValue("is_open")?.toBoolean() ?: false
        beanPath = p0.getAttributeValue("bean_path") ?: ""
        retrofitPath = p0.getAttributeValue("retrofit_path") ?: ""
        retrofitInterface = p0.getAttributeValue("retrofit_interface") ?: ""
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MVVMSetting {
            return ServiceManager.getService(project, MVVMSetting::class.java)
        }
    }
}