package com.example.sparks

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

@Serializable
data class LogData(var date:String, var cost:String, var loc:String, var len: String)

object LogDataSupplier{
    var logData = mutableListOf<LogData>()

    fun init(applicationContext: Context) {
        val lsFile = File(applicationContext.getExternalFilesDir(null)!!.path, "ls.logs")

        if (!lsFile.exists())
            loadTestLogData(applicationContext, lsFile)
        else {
            val tmp = Json {}
                .decodeFromJsonElement<List<LogData>>(
                    Json {}
                        .parseToJsonElement(lsFile.readText())
                )

            logData.addAll(tmp)
        }
    }

    fun loadTestLogData(applicationContext: Context, lsFile: File) {
        logData = mutableListOf<LogData>(LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"),
            LogData("datum", "cijena", "lokacija", "period"))

        val tmp = Json{}
            .encodeToString(
            ListSerializer(LogData.serializer()),
            logData)

        lsFile.createNewFile()
        lsFile.writeText(tmp)
    }

    fun addLog(date: String, cost: String, loc: String, len: String){
        val toAdd = LogData(date, cost, loc, len)
        logData.add(toAdd)
    }
}