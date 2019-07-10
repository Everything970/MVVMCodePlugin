package pers.chenan.code.ui

import pers.chenan.code.code.TemplateCode
import pers.chenan.code.setting.PluginSetting
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import pers.chenan.code.util.BeanCodeHelper
import pers.chenan.code.util.Utils
import pers.chenan.code.util.document
import pers.chenan.code.util.findFileByUrl
import java.util.regex.Pattern
import javax.swing.JComboBox
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MVVMSettingUIHelper(private val project: Project) {
    private val setting = PluginSetting.getInstance(project)
    private val helper = BeanCodeHelper()

    val listRequest = arrayOf("Bean", "Body", "Field", "Null")
    val listResult = arrayOf("Bean", "Map", "String", "Int", "Long", "Double", "Float", "Null")

    val listBean: List<String> by lazy {
        JavaPsiFacade.getInstance(project).findPackage(setting.beanPackagePath)?.getFiles(GlobalSearchScope.allScope(project))?.let { arrayOfPsiFiles ->
            arrayOfPsiFiles.filter { it.name.endsWith(".kt") }.map { it.name }
        } ?: listOf()
    }

    private val interfaceFunCode = setting.interfaceFunCode

    var textFieldURL: JTextField? = null
    var textAreaRequestJson: JTextArea? = null
    var textAreaResultJson: JTextArea? = null

    var textFieldFunName: JTextField? = null
    var textFieldRequestName: JTextField? = null
    var textFieldResultName: JTextField? = null

    var textAreaFunCode: JTextArea? = null
    var textAreaRequest: JTextArea? = null
    var textAreaResult: JTextArea? = null


    var comboBoxRequestType: JComboBox<String>? = null
    var comboBoxResultType: JComboBox<String>? = null
    var comboBoxInterface: JComboBox<String>? = null

    val funURL: String
        get() = textFieldURL?.text ?: ""
    val funName: String
        get() = textFieldFunName?.text ?: ""
    val requestName: String
        get() = textFieldRequestName?.text ?: ""
    val resultName: String
        get() = textFieldResultName?.text ?: ""

    val requestType: String
        get() = comboBoxRequestType?.selectedItem?.toString() ?: ""
    val resultType: String
        get() = comboBoxResultType?.selectedItem?.toString() ?: ""

    val requestDocument: String
        get() = textAreaRequestJson?.text ?: ""
    val resultDocument: String
        get() = textAreaResultJson?.text ?: ""

    var javadocStr = ""
    var requestParameter: String = ""
    var resultBean: String = ""

    val funCode: String
        get() = textAreaFunCode?.text ?: ""
    val requestContent: String
        get() = textAreaRequest?.text ?: ""
    val resultContent: String
        get() = textAreaResult?.text ?: ""


    fun bindTextArea(requestDocument: JTextArea, resultDocument: JTextArea, funCode: JTextArea, request: JTextArea, result: JTextArea) {
        textAreaRequestJson = requestDocument
        textAreaResultJson = resultDocument
        textAreaFunCode = funCode
        textAreaRequest = request
        textAreaResult = result
        requestDocument.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                println("requestDocument:$text")
                makeRequest()
            }
        })
        resultDocument.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                println("resultDocument:$text")
                makeResult()
            }
        })
    }


    fun setURLDocumentListener(tfURL: JTextField, tfFunName: JTextField, tfRequestName: JTextField, tfResultName: JTextField) {
        textFieldURL = tfURL
        textFieldFunName = tfFunName
        textFieldRequestName = tfRequestName
        textFieldResultName = tfResultName
        tfRequestName.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                makeRequest()
            }
        })
        tfResultName.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                makeResult()
            }
        })
        tfFunName.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                if (text.isNotEmpty()) {
                    var beanName = if (arrayOf("get", "set", "Get", "Set").any { text.startsWith(it) }) {
                        text.substring(3).let {
                            if (it.isEmpty()) text
                            else it
                        }
                    } else {
                        text
                    }
                    beanName = beanName.replaceRange(0, 1, beanName[0].toUpperCase().toString())
                    textFieldRequestName?.text = beanName + "Request"
                    textFieldResultName?.text = beanName + "Bean"
                } else {
                    textFieldRequestName?.text = ""
                    textFieldResultName?.text = ""
                }
            }
        })
        tfURL.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent, text: String) {
                var start = text.lastIndexOf('/')
                if (start < 0) start = text.lastIndexOf('\\')
                textFieldFunName?.text = if (start < 0) text
                else text.substring(start + 1)
            }
        })
    }

    fun bindComboBox(cbInterface: JComboBox<String>, cbRequestType: JComboBox<String>, cbResultType: JComboBox<String>) {
        comboBoxRequestType = cbRequestType
        comboBoxResultType = cbResultType
        comboBoxInterface=cbInterface
        PsiManager.getInstance(project).findFileByUrl(setting.retrofitPath)?.document?.let { document ->
            val p = Pattern.compile("(interface\\s\\w*)")
            p.matcher(document.text)?.let { matcher ->
                while (matcher.find()) {
                    cbInterface.addItem(matcher.group(1))
                }
            }
        }
        cbInterface.selectedItem = setting.retrofitInterface
        cbRequestType.addActionListener {
            makeRequest()
        }
        cbResultType.addActionListener {
            makeResult()
        }
    }

    fun check(): Boolean {
        when {
            funURL.isEmpty() -> Utils.showError("请输入请求路径")
            funName.isEmpty() -> Utils.showError("方法名不能为空")
            requestName.isEmpty() -> Utils.showError("请求参数名不能为空")
            resultName.isEmpty() -> Utils.showError("返回数据名不能为空")
            requestType == "Bean" && requestContent == "Error" -> Utils.showError("请求参数错误")
            requestType == "Filed" && requestParameter == "Error" -> Utils.showError("请求参数错误")
            resultType == "Bean" && resultContent == "Error" -> Utils.showError("返回数据错误")
            else -> return true
        }
        return false
    }

    private fun makeRequest() {
        when {
            requestType == "Bean" -> {//Bean
                textAreaRequest?.text = try {
                    helper.getBeanString(requestDocument, setting.beanPackagePath.substring(setting.beanPackagePath.indexOf("java") + 5), requestName)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error"
                }
                requestParameter = "\n\t@Body request:$requestName\n"
            }
            requestType == "Body" -> {//Body
                textAreaRequest?.text = ""
                requestParameter = "\n\t@Body request: RequestBody\n"
            }
            requestType == "Field" -> {//Field
                textAreaRequest?.text = ""
                requestParameter = helper.getFieldContent(requestDocument)
            }
            requestType.endsWith(".kt") -> {
                textAreaRequest?.text = ""
                requestParameter = "\n\t@Body request:${requestType.substring(0, requestType.length - 3)}\n"
            }
            else -> {
                textAreaRequest?.text = ""
                requestParameter = ""
            }
        }
        makeFunCode()
    }

    private fun makeResult() {
        when {
            resultType == "Bean" -> {//Bean
                textAreaResult?.text = try {
                    helper.getBeanString(resultDocument, setting.beanPackagePath.substring(setting.beanPackagePath.indexOf("java") + 5), resultName)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Error"
                }
                resultBean = requestName
            }
            resultType == "Map" -> {//Map
                textAreaResult?.text = ""
                resultBean = "Map<String,String>"
            }
            resultType == "Null" -> {
                textAreaResult?.text = ""
                resultBean = ""
            }
            resultType.endsWith(".kt") -> {
                textAreaResult?.text = ""
                resultBean = resultType.substring(0, resultType.length - 3)
            }
            else -> {
                textAreaResult?.text = ""
                resultBean = requestType
            }
        }
        makeFunCode()
    }

    private fun makeFunCode() {
        textAreaFunCode?.text = interfaceFunCode.replace(TemplateCode.javadoc, javadocStr)
                .replace(TemplateCode.interfaceURL, funURL)
                .replace(TemplateCode.funName, funName)
                .replace(TemplateCode.requestParameter, requestParameter)
                .let {
                    if (requestType != "Field") {
                        it.replace("@FormUrlEncoded\n", "")
                    } else {
                        it
                    }
                }.let {
                    if (resultBean.isEmpty()) it.replaceRange(it.lastIndexOf(":Call") + 5, it.length, "<NoDataEntity>")
                    else it.replace(TemplateCode.resultBean, resultBean)
                }
    }


    abstract class SimpleDocumentListener : DocumentListener {

        abstract fun onUpdate(e: DocumentEvent, text: String)

        override fun changedUpdate(e: DocumentEvent) {
            onUpdate(e, e.document.getText(0, e.document.length) ?: "")
        }

        override fun insertUpdate(e: DocumentEvent) {
            onUpdate(e, e.document.getText(0, e.document.length) ?: "")
        }

        override fun removeUpdate(e: DocumentEvent) {
            onUpdate(e, e.document.getText(0, e.document.length) ?: "")
        }
    }
}