package fake.domain.adamlopresto.godo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

const val PERMISSION_SEND_COMMAND = "net.dinglisch.android.tasker.PERMISSION_SEND_COMMAND"
private const val EXTRA_COMMAND = "command"
val Context.canSendTaskerCommand get() = if (VERSION.SDK_INT < VERSION_CODES.M) true else checkSelfPermission(PERMISSION_SEND_COMMAND) == PackageManager.PERMISSION_GRANTED
fun Context.sendTaskerCommand(command: String) {
    if (command.isEmpty()) throw RuntimeException("Empty command")
    if (!canSendTaskerCommand) throw SecurityException("No permission to send Tasker Command")

    val intent = Intent().apply {
        setClassName("net.dinglisch.android.taskerm", "com.joaomgcd.taskerm.command.ServiceSendCommand")
        putExtra(EXTRA_COMMAND, command)
    }
    try {
        if (VERSION.SDK_INT < VERSION_CODES.O) startService(intent) else startForegroundService(intent)
    } catch (t: Throwable) {
        throw RuntimeException("Couldn't send command", t)
    }
}

fun Context.sendTaskerCommandNoExceptions(command: String) = try {
    sendTaskerCommand(command)
    true
} catch (t: Throwable) {
    false
}