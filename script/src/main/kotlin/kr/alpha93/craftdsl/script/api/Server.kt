package kr.alpha93.craftdsl.script.api

import org.bukkit.Bukkit
import org.bukkit.Server

val server: Server
    get() = Bukkit.getServer()
