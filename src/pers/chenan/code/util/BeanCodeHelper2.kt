package pers.chenan.code.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.parser.Feature

class BeanCodeHelper2 {
    @Throws(Exception::class)
    fun getBeanString(json: String, packageName: String, beanName: String): String {
        val jsonObject = JSON.parseObject(json, Feature.OrderedField)
        val sb = StringBuilder()
        sb.append("package $packageName").append('\n')
                .append("class $beanName {").append('\n')

        if (jsonObject == null || jsonObject.isEmpty()) {
            sb.append("}\n")
        } else {
            jsonObject.forEach { t, u ->
                sb.append(analysisJsonObject(t, u))
            }
            sb.append("}\n")
        }
        return sb.toString()

    }


    private fun analysisJsonObject(key: String, any: Any): String {
        val sb = StringBuilder()
        when (any) {
            is String -> {
                sb.append("var $key : String=\"\"\n")
            }
            is Int -> {
                sb.append("var $key : Int=0\n")
            }
            is JSONObject -> {
                val name = formatClassName(key)
                sb.append("var  $key : $name = $name() \n")
                sb.append("class $name {\n")
                any.forEach { t, u ->
                    sb.append(analysisJsonObject(t, u))
                }
                sb.append("}\n")
            }
            is JSONArray -> {
                if (any.size == 0) {
                    sb.append("val $key : List<Any> =listOf()\n")
                } else {
                    when (any[0]) {
                        is Int -> {
                            sb.append("val $key : List<Int> =listOf()\n")
                        }
                        is String -> {
                            sb.append("val $key : List<String> =listOf()\n")
                        }
                        is JSONObject -> {
                            val name = formatClassName(key)
                            sb.append("val $key : List<$name> =listOf()\n")
                            sb.append("class $name {\n")
                            (any[0] as JSONObject).forEach { t, u ->
                                sb.append(analysisJsonObject(t, u))
                            }
                            sb.append("}\n")
                        }
                    }

                }
            }
        }

        return sb.toString()
    }

    private fun formatClassName(str: String): String {
        val reg = Regex("[^a-zA-Z]")
        val sb = StringBuffer(str)
        sb.setCharAt(0, str[0].toUpperCase())
        return sb.replace(reg,"")
    }

}