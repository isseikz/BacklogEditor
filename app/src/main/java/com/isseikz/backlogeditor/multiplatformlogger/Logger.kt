package com.isseikz.backlogeditor.multiplatformlogger

interface Logger {
    fun d(message: String)
    fun e(message: String)
    fun i(message: String)
    fun v(message: String)
    fun w(message: String)
    fun wtf(message: String)
    fun e(throwable: Throwable)
}
