package com.example.sparks

import android.content.Context
import android.widget.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

@Serializable
data class LogData(var date:String, var cost:String, var loc:String)

object LogDataSupplier{
    var logData = mutableListOf<LogData>(LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"),
        LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
        , LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
        , LogData("datum", "cijena", "lokacija"))

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
        logData = mutableListOf<LogData>(LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"),
            LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
            , LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
            , LogData("datum", "cijena", "lokacija"))


        val tmp = Json{
            isLenient = true
        }.encodeToString(
            ListSerializer(LogData.serializer()),
            logData
        )

        lsFile.writeText(tmp)

    }
}