package pers.chenan.code.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlin.text.StringBuilder

class BeanCodeHelper {
    @Throws(Exception::class)
    fun getBeanString(json: String, packageName: String, beanName: String): String {

        val dependencyList = arrayListOf<String>()
        val innerList = arrayListOf<Pair<String, JsonObject>>()

        val jsonObject = Gson().fromJson<JsonObject>(json, JsonObject::class.java)
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
            val jsonObject = Gson().fromJson<JsonObject>(json, JsonObject::class.java)
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

    fun getType(jsonElement: JsonElement, key: String, innerList: ArrayList<Pair<String, JsonObject>>): Type {
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
                val je = jsonElement.asJsonArray[0]
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
}