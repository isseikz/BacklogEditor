package com.isseikz.backlogeditor.multiplatformlogger

import timber.log.Timber

object AndroidLogger: Logger {
    override fun d(message: String) {
        Timber.d(message)
    }

    override fun e(message: String) {
        Timber.e(message)
    }

    override fun e(throwable: Throwable) {
        Timber.e(throwable)
    }

    override fun i(message: String) {
        Timber.i(message)
    }

    override fun v(message: String) {
        Timber.v(message)
    }

    override fun w(message: String) {
        Timber.w(message)
    }

    override fun wtf(message: String) {
        Timber.wtf(message)
    }
}
