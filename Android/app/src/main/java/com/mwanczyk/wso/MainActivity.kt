package com.mwanczyk.wso

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val folder = filesDir
        val f = File(folder, "WSO")
        f.mkdir()
        val folderPath = folder.toString()
        val btnClick = findViewById<Button>(R.id.btn_connect)
        btnClick.setOnClickListener {
            findViewById<TextView>(R.id.output).text = "Wait..."
            val ipAddress = findViewById<EditText>(R.id.ip).text.toString()
            val userName = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            SshTask().execute(userName, password, ipAddress)
        }
    }

    private inner class SshTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            print("test" + params[0])
            val output = executeRemoteCommand(username=params[0], password=params[1], hostname=params[2])
            print(output)
            return output
        }

        override fun onPostExecute(result: String?) {
            val file = File("/data/user/0/com.mwanczyk.wso/files/output.txt").readText(Charsets.UTF_8)
            findViewById<TextView>(R.id.output).text = file
            println(file)
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
    sshChannel.setCommand("mv WSO/.output.txt WSO/.output-old.txt")
//    sshChannel.setCommand("ls -la")
    sshChannel.connect()

    // Sleep needed in order to wait long enough to get result back.
    Thread.sleep(1_000)
    sshChannel.disconnect()

    val channel: Channel = session.openChannel("sftp")
    channel.connect();
    val sftpChannel = channel as ChannelSftp


    sftpChannel.get("WSO/.output-old.txt", "/data/user/0/com.mwanczyk.wso/files/output.txt")
    sftpChannel.exit()

    session.disconnect()
    println("reading.... " + "/data/user/0/com.mwanczyk.wso/files/output.txt")
    val file = File("/data/user/0/com.mwanczyk.wso/files/output.txt").readText(Charsets.UTF_8)

    return file
}
