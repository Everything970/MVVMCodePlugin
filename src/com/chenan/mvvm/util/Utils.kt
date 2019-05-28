package com.chenan.mvvm.util

import com.chenan.mvvm.code.TemplateCode
import com.chenan.mvvm.data.Config
import com.google.gson.Gson
import com.intellij.openapi.ui.DialogBuilder
import java.io.*
import javax.swing.filechooser.FileSystemView

object Utils {
    private const val pluginName = "MVVMPlugin"
    const val defaultActivity = "activity_code"
    const val defaultViewModel = "view_model_code"
    const val defaultLayout = "layout_code"

    fun getSaveUrl(): File {
        val fsv = FileSystemView.getFileSystemView()
        return fsv.defaultDirectory
    }

    fun getPluginPath(): File {
        val file = File(getSaveUrl(), pluginName)
        if (!file.exists()) {
            file.mkdirs()
            createCode(file, TemplateCode.TYPE_ACTIVITY, "$defaultActivity.txt", TemplateCode.activityCode)
            createCode(file, TemplateCode.TYPE_VIEW_MODEL, "$defaultViewModel.txt", TemplateCode.viewModelCode)
            createCode(file, TemplateCode.TYPE_LAYOUT, "$defaultLayout.txt", TemplateCode.layoutCode)
            saveConfig(file, Config().apply {
                lateActivity = defaultActivity
                lateViewModel = defaultViewModel
                lateLayout = defaultLayout
            })
        }
        return file
    }


    @JvmStatic
    fun getActivityFiles(): List<File> {
        return File(getPluginPath(), TemplateCode.TYPE_ACTIVITY).listFiles().filter { it.isFile && it.name.endsWith(".txt") }
    }

    @JvmStatic
    fun getViewModelFiles(): List<File> {
        return File(getPluginPath(), TemplateCode.TYPE_VIEW_MODEL).listFiles().filter { it.isFile && it.name.endsWith(".txt") }
    }

    @JvmStatic
    fun getLayoutFiles(): List<File> {
        return File(getPluginPath(), TemplateCode.TYPE_LAYOUT).listFiles().filter { it.isFile && it.name.endsWith(".txt") }
    }

    @JvmStatic
    fun getConfig(): Config {
        return Config.getInstance()
    }

    @JvmStatic
    fun saveConfig(config: Config) {
        saveConfig(getPluginPath(), config)
    }

    fun saveConfig(parentFile: File, config: Config) {
        val file = File(parentFile, "config.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        var fw: FileWriter? = null
        var bw: BufferedWriter? = null
        try {
            fw = FileWriter(file, false)
            bw = BufferedWriter(fw)
            bw.write(Gson().toJson(config))
        } catch (e: Exception) {

        } finally {
            bw?.close()
            fw?.close()
        }
    }

    fun createCode(parentFile: File, type: String, name: String, content: String): File? {
        val file = File(parentFile, type)
        if (!file.exists()) {
            file.mkdirs()
        }
        val txt = File(file, name)
        if (!txt.exists()) {
            txt.createNewFile()
        }
        var fw: FileWriter? = null
        var bw: BufferedWriter? = null
        return try {
            fw = FileWriter(txt, false)
            bw = BufferedWriter(fw)
            bw.write(content)
            bw.close()
            fw.close()
            txt
        } catch (e: Exception) {
            bw?.close()
            fw?.close()
            null
        }
    }

    fun getCode(name: String): String {
        val file = File(getPluginPath(), name)
        return if (!file.exists()) {
            ""
        } else {
            try {
                val fis = FileInputStream(file)
                val isr = InputStreamReader(fis, Charsets.UTF_8)
                val br = BufferedReader(isr)
                val sb = StringBuffer()
                while (true) {
                    val text = br.readLine() ?: break
                    sb.append(text).append("\n")
                }
                sb.toString()
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun getLowerActivityName(activityName: String): String {
        val lowerName = StringBuffer()
        activityName.forEach {
            if (it.isLowerCase()) {
                lowerName.append(it)
            } else {
                lowerName.append('_').append(it.toLowerCase())
            }
        }
        return lowerName.toString()
    }

    fun getCodeContent(templateCode: String, packageName: String, activityName: String): String {
        return templateCode.replace(TemplateCode.packageName, packageName)
                .replace(TemplateCode.activityName, activityName)
                .replace(TemplateCode.lowerActivityName, getLowerActivityName(activityName))
    }

    @JvmStatic
    fun showError(error: String) {
        DialogBuilder().apply {
            setTitle("提示")
            setErrorText(error)
        }.show()
    }
}