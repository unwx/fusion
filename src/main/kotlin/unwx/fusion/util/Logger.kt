package unwx.fusion.util

object Logger {
    inline fun info(msg: () -> String) {
        log(Level.INFO, msg)
    }

    inline fun warn(msg: () -> String) {
        log(Level.WARN, msg)
    }

    inline fun error(msg: () -> String) {
        log(Level.ERROR, msg)
    }

    inline fun log(level: Level, msg: () -> String) {
        println("[Fusion] [${level.name}] [${msg()}]")
    }

    enum class Level {
        INFO,
        WARN,
        ERROR
    }
}
