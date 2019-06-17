package pers.chenan.code.setting

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import org.jdom.Element
import pers.chenan.code.code.TemplateCode
import pers.chenan.code.ui.WriteCodeDialog
import pers.chenan.code.util.Utils
import javax.swing.JComboBox

class MVVMSetting: PersistentStateComponent<Element> {

    val activityMap: HashMap<String, String> = hashMapOf()
    val viewModelMap: HashMap<String, String> = hashMapOf()
    val layoutMap: HashMap<String, String> = hashMapOf()

    var isNeedConfirm: Boolean = true
    var activity: String = Utils.defaultActivity
    var viewModel: String = Utils.defaultViewModel
    var layout: String = Utils.defaultLayout

    val activityCode: String?
        get() = if (activity == Utils.defaultActivity) TemplateCode.activityCode else activityMap[activity]
    val viewModelCode: String?
        get() = if (viewModel == Utils.defaultViewModel) TemplateCode.viewModelCode else viewModelMap[viewModel]
    val layoutCode: String?
        get() = if (layout == Utils.defaultLayout) TemplateCode.layoutCode else layoutMap[layout]

    override fun getState(): Element? {
        return Element("MVVMSetting").apply {
            setAttribute("is_need_confirm", isNeedConfirm.toString())
            setAttribute("activity", activity)
            setAttribute("view_model", viewModel)
            setAttribute("layout", layout)
            setAttribute("code_map", Gson().toJson(listOf(activityMap, viewModelMap, layoutMap)))
        }
    }

    override fun loadState(p0: Element) {
        isNeedConfirm = p0.getAttributeValue("is_need_confirm")?.toBoolean() ?: true
        activity = p0.getAttributeValue("activity") ?: Utils.defaultActivity
        viewModel = p0.getAttributeValue("view_model") ?: Utils.defaultViewModel
        layout = p0.getAttributeValue("layout") ?: Utils.defaultLayout
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
    fun containsActivity(activity: String): Boolean {
        return activity == Utils.defaultActivity || activityMap.containsKey(activity)
    }

    fun containsViewModel(viewModel: String): Boolean {
        return viewModel == Utils.defaultViewModel || viewModelMap.containsKey(viewModel)
    }

    fun containsLayout(layout: String): Boolean {
        return layout == Utils.defaultLayout || layoutMap.containsKey(layout)
    }

    private fun containsCode(item: String, type: Int): Boolean {
        return when (type) {
            0 -> containsActivity(item)
            1 -> containsViewModel(item)
            2 -> containsLayout(item)
            else -> false
        }
    }

    private fun getCode(item: String, type: Int): String {
        return when (type) {
            0 -> activityMap.getOrDefault(item, "")
            1 -> viewModelMap.getOrDefault(item, "")
            2 -> layoutMap.getOrDefault(item, "")
            else -> ""
        }
    }

    /**
     * 编辑模板代码
     * @param comboBox 所属选择器
     * @param type 0:activity 1:viewModel 2:layout
     */
    fun editTemplateCode(comboBox: JComboBox<String>, type: Int) {
        val item = comboBox.selectedItem
        if ((type == 0 && Utils.defaultActivity == item)
                || (type == 1 && Utils.defaultViewModel == item)
                || (type == 2 && Utils.defaultLayout == item)) {
            Utils.showError("默认模板不可编辑")
            return
        }
        WriteCodeDialog().also {
            it.title = when (type) {
                0 -> "编辑Activity模板代码"
                1 -> "编辑ViewModel模板代码"
                else -> "编辑Layout模板代码"
            }
            it.setListener { name, content ->
                if (name == item) {
                    when (type) {
                        0 -> activityMap[name] = content
                        1 -> viewModelMap[name] = content
                        2 -> layoutMap[name] = content
                    }
                    it.dispose()
                } else {
                    if (containsCode(name, type)) {
                        Utils.showError("已存在相同命名模板")
                    } else {
                        when (type) {
                            0 -> {
                                activityMap[name] = content
                                activityMap.remove(item.toString())
                            }
                            1 -> {
                                viewModelMap[name] = content
                                viewModelMap.remove(item.toString())
                            }
                            2 -> {
                                layoutMap[name] = content
                                layoutMap.remove(item.toString())
                            }
                        }
                        comboBox.addItem(name)
                        comboBox.selectedItem = name
                        comboBox.removeItem(item)
                        it.dispose()
                    }
                }
            }
        }.showDialog(item.toString(), getCode(item.toString(), type))
    }

    /**
     * 添加模板代码
     * @param comboBox 所属选择器
     * @param type 0:activity 1:viewModel 2:layout
     */
    fun addTemplateCode(comboBox: JComboBox<String>, type: Int) {
        WriteCodeDialog().also {
            it.title = when (type) {
                0 -> "添加Activity模板代码"
                1 -> "添加ViewModel模板代码"
                else -> "添加Layout模板代码"
            }
            it.setListener { name, content ->
                if (containsCode(name, type)) {
                    Utils.showError("已存在相同命名模板")
                } else {
                    when (type) {
                        0 -> activityMap[name] = content
                        1 -> viewModelMap[name] = content
                        2 -> layoutMap[name] = content
                    }
                    comboBox.addItem(name)
                    comboBox.selectedItem = name
                    it.dispose()
                }
            }
        }.showDialog("", when (type) {
            0 -> TemplateCode.activityCode
            1 -> TemplateCode.viewModelCode
            else -> TemplateCode.layoutCode
        })
    }


    /**
     * 删除模板代码
     * @param comboBox 所属选择器
     * @param type 0:activity 1:viewModel 2:layout
     */
    fun deleteTemplateCode(comboBox: JComboBox<String>, type: Int) {
        val item = comboBox.selectedItem
        if ((type == 0 && Utils.defaultActivity == item)
                || (type == 1 && Utils.defaultViewModel == item)
                || (type == 2 && Utils.defaultLayout == item)) {
            Utils.showError("默认模板不可删除")
            return
        }
        JBPopupFactory.getInstance().createConfirmation("确认删除${item}吗？", "确定", "取消", {
            when (type) {
                0 -> activityMap.remove(item.toString())
                1 -> viewModelMap.remove(item.toString())
                2 -> layoutMap.remove(item.toString())
            }
            comboBox.removeItem(item)
        }, 0).showInFocusCenter()
    }

    companion object {
        @JvmStatic
        fun getInstance(): MVVMSetting {
            return ServiceManager.getService(MVVMSetting::class.java)
        }
    }
}