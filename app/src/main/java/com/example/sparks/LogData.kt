package com.example.sparks

data class LogData(var date:String, var cost:String, var loc:String)

object Supplier{
    var logData = listOf<LogData>(LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"),
        LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
        , LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija"), LogData("datum", "cijena", "lokacija")
        , LogData("datum", "cijena", "lokacija"))
}