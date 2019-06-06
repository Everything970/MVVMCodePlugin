package com.chenan.mvvm.util

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.setting.MVVMSetting
import com.intellij.openapi.project.Project
import javax.swing.JComboBox
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FunCodeHelper(project: Project) {
    private val setting = MVVMSetting.getInstance(project)
    private val helper = BeanCodeHelper()

    val listRequest = arrayOf("Bean", "Body", "Field", "Null")
    val listResult = arrayOf("Bean", "Map", "String", "Int", "Long", "Double", "Float", "Null")

    private val interfaceFunCode = setting.interfaceFunCode

    var funName: String = ""
        set(value) {
            if (field != value) {
                textFieldFunName?.text = value
            }
            field = value
            var beanName = if (
                    arrayOf("get", "set", "Get", "Set").any { value.startsWith(it) }
            ) {
                value.substring(3).let {
                    if (it.isEmpty()) value
                    else it
                }
            } else {
                value
            }
            beanName = beanName.replaceRange(0, 1, beanName[0].toUpperCase().toString())
            requestName = beanName + "Request"
            resultName = beanName + "Bean"
        }
    var requestName: String = ""
        set(value) {
            if (value != field) {
                textFieldRequestName?.text = value
            }
            field = value
        }
    var resultName: String = ""
        set(value) {
            if (value != field) {
                textFieldResultName?.text = value
            }
            field = value
        }

    var requestType: String = ""
    var resultType: String = ""
    var requestDocument: String = ""
    var resultDocument: String = ""

    var javadocStr = ""
        set(value) {
            if (field != value) {
                makeFunCode()
            }
            field = value
        }
    var requestParameter: String = ""
        set(value) {
            if (field != value) {
                makeFunCode()
            }
            field = value
        }
    var resultBean: String = ""
        set(value) {
            if (field != value) {
                makeFunCode()
            }
            field = value
        }
    var requestContent: String = ""
        set(value) {
            if (value != field) {
                textAreaRequest?.text = field
            }
            field = value
        }
    var resultContent: String = ""
        set(value) {
            if (value != field) {
                textAreaResult?.text = field
            }
            field = value
        }

    var funCode: String = ""
        set(value) {
            if (value != field) {
                textAreaFunCode?.text = field
            }
            field = value
        }

    var textAreaRequest: JTextArea? = null
    var textAreaResult: JTextArea? = null
    var textAreaFunCode: JTextArea? = null

    var textFieldFunName: JTextField? = null
    var textFieldRequestName: JTextField? = null
    var textFieldResultName: JTextField? = null

    fun bindTextArea(funCode: JTextArea, request: JTextArea, result: JTextArea) {
        textAreaFunCode = funCode
        textAreaRequest = request
        textAreaResult = result
    }

    fun bindTextField(funName: JTextField, requestName: JTextField, resultName: JTextField) {
        textFieldFunName = funName
        textFieldRequestName = requestName
        textFieldResultName = resultName
    }


    fun setURLDocumentListener(jTextField: JTextField) {
        println("jTextField:$jTextField")
        jTextField.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent) {
                val url = e.document.getText(0, e.document.length)
                println("setURLDocumentListener url:$url")
                if (!url.isNullOrEmpty()) {
                    var start = url.lastIndexOf('/')
                    if (start < 0) start = url.lastIndexOf('\\')
                    funName = if (start < 0) {
                        url
                    } else {
                        url.substring(start + 1)
                    }
                }
            }
        })
    }

    fun setRequestTypeListener(jComboBox: JComboBox<String>) {
        jComboBox.addActionListener { actionEvent ->
            requestType = (actionEvent.source as? JComboBox<*>)?.selectedItem as? String ?: return@addActionListener
        }
    }

    fun setRequestDocumentListener(jTextArea: JTextArea) {
        jTextArea.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent) {
                requestDocument = e.document.getText(0, e.document.length)
            }
        })
    }

    fun setResultTypeListener(jComboBox: JComboBox<String>) {
        jComboBox.addActionListener { actionEvent ->
            resultType = (actionEvent.source as? JComboBox<*>)?.selectedItem as? String ?: return@addActionListener
        }
    }

    fun setResultDocumentListener(jTextArea: JTextArea) {
        jTextArea.document.addDocumentListener(object : SimpleDocumentListener() {
            override fun onUpdate(e: DocumentEvent) {
                resultDocument = e.document.getText(0, e.document.length)
            }
        })
    }

    private fun makeRequest() {
        requestParameter = when (requestType) {
            listRequest[0] -> {
                requestContent = helper.getBeanString(requestDocument, setting.beanPath.substring(setting.beanPath.indexOf("java") + 5), requestName)
                "\n@Body request:$requestName"
            }
            listRequest[1] -> {
                requestContent = ""
                "\n@Body request: RequestBody\n"
            }
            listRequest[2] -> {
                requestContent = ""
                helper.getFieldContent(requestDocument)
            }
            else -> {
                requestContent = ""
                ""
            }
        }
    }

    private fun makeResult() {
        resultBean = when (requestType) {
            listResult[0] -> {
                resultContent = helper.getBeanString(resultDocument, setting.beanPath.substring(setting.beanPath.indexOf("java") + 5), resultName)
                requestName
            }
            listResult[1] -> {
                requestContent = ""
                "Map<String,String>"
            }
            listResult[7] -> {
                requestContent = ""
                ""
            }
            else -> {
                requestContent = ""
                requestType
            }
        }
    }

    private fun makeFunCode() {
        funCode = interfaceFunCode.replace(TemplateCode.javadoc, javadocStr)
                .replace(TemplateCode.funName, funName)
                .replace(TemplateCode.requestParameter, requestParameter)
                .let {
                    if (resultBean.isEmpty()) it.replaceRange(it.lastIndexOf(":Call") + 5, it.length, "<NoDataEntity>")
                    else it.replace(TemplateCode.resultBean, resultBean)
                }
    }


    abstract class SimpleDocumentListener : DocumentListener {

        abstract fun onUpdate(e: DocumentEvent)

        override fun changedUpdate(e: DocumentEvent) {
            onUpdate(e)
        }

        override fun insertUpdate(e: DocumentEvent) {
            onUpdate(e)
        }

        override fun removeUpdate(e: DocumentEvent) {
            onUpdate(e)
        }
    }
}