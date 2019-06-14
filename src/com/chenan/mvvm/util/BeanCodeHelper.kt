package com.chenan.mvvm.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.net.CacheRequest

class BeanCodeHelper {
    @Throws(Exception::class)
    fun getBeanString(json: String, packageName: String, beanName: String): String {

        val dependencyList = arrayListOf<String>()
        val innerList = arrayListOf<Pair<String, JsonObject>>()

        val jsonObject = Gson().fromJson<JsonObject>(json, JsonObject::class.java)
        val sb = StringBuffer()
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
        return sb.toString().let { str ->
            str.replace("#dependencyClass", dependencyList.joinToString("\n") {
                "import $it"
            }).replace("#innerClass", innerList.joinToString("\n") {
                val c = StringBuffer()
                c.append("class ${it.first}{").append('\n')
                it.second.keySet().forEach { key ->
                    val type = getType(it.second.get(key), key, innerList)
                    if (type.flag == 0) {
                        c.append("\t//")
                    } else {
                        c.append("\t")
                    }
                    c.append("var $key: ${type.name} = ${type.value}").append('\n')
                }
                c.append("}").append('\n')
                c.toString()
            })
        }
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
                    jeStr.toDoubleOrNull() != null -> Type(3, "Double", "0.0")
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