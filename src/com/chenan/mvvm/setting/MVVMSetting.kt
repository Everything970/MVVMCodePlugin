package com.chenan.mvvm.setting

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import org.jdom.Element

class MVVMSetting : PersistentStateComponent<Element> {

    var activity: String = Utils.defaultActivity
    var viewModel: String = Utils.defaultViewModel
    var layout: String = Utils.defaultLayout

    val activityMap: HashMap<String, String> = hashMapOf()
    val viewModelMap: HashMap<String, String> = hashMapOf()
    val layoutMap: HashMap<String, String> = hashMapOf()

    var isOpen: Boolean = false
    var beanPackagePath: String = ""
    var retrofitPath: String = ""
    var retrofitInterface: String = ""
    var interfaceFunCode: String = TemplateCode.interfaceFunCode

    val activityCode: String?
        get() = if (activity == Utils.defaultActivity) TemplateCode.activityCode else activityMap[activity]
    val viewModelCode: String?
        get() = if (viewModel == Utils.defaultViewModel) TemplateCode.viewModelCode else viewModelMap[viewModel]
    val layoutCode: String?
        get() = if (layout == Utils.defaultLayout) TemplateCode.layoutCode else layoutMap[layout]

    override fun getState(): Element? {
        return Element("MVVMSetting").apply {
            setAttribute("activity", activity)
            setAttribute("view_model", viewModel)
            setAttribute("layout", layout)
            setAttribute("is_open", isOpen.toString())
            setAttribute("bean_path", beanPackagePath)
            setAttribute("retrofit_path", retrofitPath)
            setAttribute("retrofit_interface", retrofitInterface)
            setAttribute("interface_fun_code", interfaceFunCode)
            setAttribute("code_map", Gson().toJson(listOf(activityMap, viewModelMap, layoutMap)))
        }
    }

    override fun loadState(p0: Element) {
        activity = p0.getAttributeValue("activity") ?: Utils.defaultActivity
        viewModel = p0.getAttributeValue("view_model") ?: Utils.defaultViewModel
        layout = p0.getAttributeValue("layout") ?: Utils.defaultLayout
        isOpen = p0.getAttributeValue("is_open")?.toBoolean() ?: false
        beanPackagePath = p0.getAttributeValue("bean_path") ?: ""
        retrofitPath = p0.getAttributeValue("retrofit_path") ?: ""
        retrofitInterface = p0.getAttributeValue("retrofit_interface") ?: ""
        interfaceFunCode = p0.getAttributeValue("interface_fun_code") ?: TemplateCode.interfaceFunCode
        val json = p0.getAttributeValue("code_map")
        if (!json.isNullOrEmpty()) {
            val list = Gson().fromJson<List<Map<String, String>>>(json, (object : TypeToken<List<Map<String, String>>>() {}).type)
            activityMap.clear()
            activityMap.putAll(list.getOrElse(0) { mapOf() })
            viewModelMap.clear()
            viewModelMap.putAll(list.getOrElse(1) { mapOf() })
            layoutMap.clear()
            layoutMap.putAll(list.getOrElse(2) { mapOf() })
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MVVMSetting {
            return ServiceManager.getService(project, MVVMSetting::class.java)
        }
    }
}