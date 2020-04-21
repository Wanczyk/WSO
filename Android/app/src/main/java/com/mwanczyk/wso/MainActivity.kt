package com.mwanczyk.wso

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        print("test")
        SshTask().execute()
    }

    class SshTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg p0: Void?): String {
            val output = executeRemoteCommand("pi", "pass", "192.168.0.22")
            print(output)
            return output
        }
    }
}

fun executeRemoteCommand(username: String,
                         password: String,
                         hostname: String,
                         port: Int = 22): String {
    val jsch = JSch()
    val session = jsch.getSession(username, hostname, port)
    session.setPassword(password)

    // Avoid asking for key confirmation.
    val properties = Properties()
    properties["StrictHostKeyChecking"] = "no"
    session.setConfig(properties)

    session.connect()

    // Create SSH Channel.
    val sshChannel = session.openChannel("exec") as ChannelExec
    val outputStream = ByteArrayOutputStream()
    sshChannel.outputStream = outputStream

    // Execute command.
    sshChannel.setCommand("ls")
    sshChannel.connect()

    // Sleep needed in order to wait long enough to get result back.
    Thread.sleep(1_000)
    sshChannel.disconnect()

    session.disconnect()

    return outputStream.toString()
}
