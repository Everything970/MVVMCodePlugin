package pers.chenan.code.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.parser.Feature
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class BeanCodeHelper {
    fun getBeanString(json: String, packageName: String, beanName: String, al: Int): String {

        return if (al == 0) getBeanString1(json, packageName, beanName) else getBeanString2(json, packageName, beanName)
    }

    @Throws(Exception::class)
    fun getBeanString1(json: String, packageName: String, beanName: String): String {

        val dependencyList = arrayListOf<String>()
        val innerList = arrayListOf<Pair<String, JsonObject>>()

        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
        val sb = StringBuilder()
        sb.append("package $packageName").append('\n')
                .append("#dependencyClass").append('\n')
                .append("class $beanName {").append('\n')
        jsonObject.keySet().forEach { key ->
            val type = getType(jsonObject.get(key), key, innerList)
            if (type.flag == 0) {
                sb.append("\t//")
            } else {
                sb.append("\t")
            }
            sb.append("var $key: ${type.name} = ${type.value}").append('\n')
        }
        sb.append("#innerClass").append('\n')
        sb.append("}")
        val isb = StringBuilder()
        var index = 0
        while (index != innerList.size) {
            val list = innerList.toList()
            val start = index
            for (i in start until list.size) {
                index = i + 1
                val data = list[i]
                isb.append("class ${data.first}{").append('\n')
                data.second.keySet().forEach { key ->
                    val type = getType(data.second.get(key), key, innerList)
                    if (type.flag == 0) {
                        isb.append("\t//")
                    } else {
                        isb.append("\t")
                    }
                    isb.append("var $key: ${type.name} = ${type.value}").append('\n')
                }
                isb.append("}").append('\n')
            }
        }
        val innerStr = isb.delete(isb.length - 1, isb.length).toString()

        return sb.toString()
                .replace("#dependencyClass", dependencyList.joinToString("\n") { "import $it" })
                .replace("#innerClass", innerStr)
    }


    fun getFieldContent(json: String): String {
        return try {
            val innerList = arrayListOf<Pair<String, JsonObject>>()
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            val sb = StringBuffer()
            sb.append("\n")
            jsonObject.keySet().forEach { key ->
                val type = getType(jsonObject.get(key), key, innerList)
                if (type.flag == 3) {
                    sb.append("\t")
                } else {
                    sb.append("\t//")
                }
                sb.append("@Field(\"$key\") $key: ${type.name},").append('\n')
            }
            sb.delete(sb.length - 2, sb.length - 1)
            sb.toString()
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun getType(jsonElement: JsonElement, key: String, innerList: ArrayList<Pair<String, JsonObject>>): Type {
        return when {
            jsonElement.isJsonNull -> {
                return Type()
            }
            jsonElement.isJsonObject -> {
                val sb = StringBuffer(key)
                sb.setCharAt(0, key[0].toUpperCase())
                val type = sb.toString()
                innerList.add(type to jsonElement.asJsonObject)
                return Type(1, type, "$type()")
            }
            jsonElement.isJsonArray -> {
                val je = jsonElement.asJsonArray.let {
                    if (it.size() == 0) null else it[0]
                } ?: return Type()
                val type = getType(je, key, innerList)
                return Type(2, "List<${type.name}>", "listOf()")
            }
            jsonElement.isJsonPrimitive -> {
                val jeStr = jsonElement.toString()
                when {
                    jeStr.startsWith('\"') -> {
                        Type(3, "String", "\"\"")
                    }
                    jeStr == "false" || jeStr == "true" -> {
                        Type(3, "Boolean", jeStr)
                    }
                    jeStr.toIntOrNull() != null || jeStr.toLongOrNull() != null -> {
                        when {
                            arrayOf("Price", "price").any { key.endsWith(it) } ->
                                Type(3, "Float", "0f")
                            arrayOf("ID", "Id", "id", "IDs", "Ids", "ids").any { key.endsWith(it) } ->
                                Type(3, "Long", "0L")
                            arrayOf("State", "state", "Type", "type", "Status", "status").any { key.contains(it) } ->
                                Type(3, "Int", "0")
                            (jeStr == "0" || jeStr == "1") && key.startsWith("is") ->
                                Type(3, "Boolean", (jeStr == "1").toString())
                            jeStr.toIntOrNull() != null ->
                                Type(3, "Int", "0")
                            else ->
                                Type(3, "Long", "0L")
                        }
                    }
                    jeStr.toDoubleOrNull() != null -> {
                        when {
                            arrayOf("Price", "price").any { key.endsWith(it) } ->
                                Type(3, "Float", "0f")
                            else ->
                                Type(3, "Double", "0.0")
                        }
                    }
                    else -> Type()
                }
            }
            else -> Type()
        }
    }

    data class Type(
            var flag: Int = 0,//0:Null 1:Object 2:List 3:Primitive
            var name: String = "Any?",
            var value: String = "null"
    )


    @Throws(Exception::class)
    fun getBeanString2(json: String, packageName: String, beanName: String): String {
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