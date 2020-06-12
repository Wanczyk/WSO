package com.mwanczyk.wso

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*


public val key_list = listOf<String>(
    "",
    "Esc",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "0",
    "-",
    "=",
    "",
    "Tab",
    "Q",
    "W",
    "E",
    "R",
    "T",
    "Y",
    "U",
    "I",
    "O",
    "P",
    "[",
    "]",
    "Return",
    "",
    "A",
    "S",
    "D",
    "F",
    "G",
    "H",
    "J",
    "K",
    "L",
    ";",
    "'",
    "`",
    "Shift Left",
    "\\",
    "Z",
    "X",
    "C",
    "V",
    "B",
    "N",
    "M",
    ",",
    ".",
    "/",
    "Shift Right",
    "KP *",
    "Alt Left (-> Command)",
    " ",
    "Caps Lock",
    "F1",
    "F2",
    "F3",
    "F4",
    "F5",
    "F6",
    "F7",
    "F8",
    "F9",
    "F10",
    "Num Lock",
    "Scroll Lock",
    "KP 7",
    "KP 8",
    "KP 9",
    "KP -",
    "KP 4",
    "KP 5",
    "KP 6",
    "KP +",
    "KP 1",
    "KP 2",
    "KP 3",
    "KP 0",
    "KP .",
    "",
    "",
    "International",
    "F11",
    "F12",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "KP Enter",
    "Ctrl Right",
    "KP /",
    "PrintScrn",
    "Alt Right (-> Command)",
    "",
    "Home",
    "Cursor Up",
    "Page Up",
    "",
    "Cursor Right",
    "End",
    "Cursor Down",
    "Page Down",
    "Insert",
    "Delete",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "Pause",
    "",
    "",
    "",
    "",
    "",
    "Logo Left (-> Option)",
    "Logo Right (-> Option)"
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        readKeysNumbers()
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
            try {
                SshTask().execute(userName, password, ipAddress)
            }
            catch(e: Exception){
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private inner class SshTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            try{
                val output = executeRemoteCommand(username=params[0], password=params[1], hostname=params[2])
                return output
            }
            catch (e: Exception) {
                println(e.toString())
            }
        return "output"
        }

        override fun onPostExecute(result: String?) {
            val inputStream: InputStream = File("/data/user/0/com.mwanczyk.wso/files/output.txt").inputStream()
            val lineList = mutableListOf<String>()
            var output : String = ""
            var to_left = 0
            var letter = ""
            inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
            lineList.forEach loop@{
                when {
                    it.toInt()==105 -> {
                        if(to_left <= output.length)
                        to_left++
                        return@loop
                    }
                    it.toInt()==106 -> {
                        if (to_left>0){
                            to_left--
                        }
                        return@loop
                    }
                }
                letter = key_list[it.toInt()]
                when {
                    to_left>0 -> {
                        if(it.toInt()==28) {
                            letter = "\n"
                        }
                        else{

                        }
                        output = if(it.toInt()==14){
                            output.substring(0, output.length - to_left - 1) + letter + output.substring(output.length - to_left, output.length)
                        } else {
                            output.substring(0, output.length - to_left) + letter + output.substring(output.length - to_left, output.length)
                        }
                    }
                    else -> {
                        if(it.toInt()==14) {
                            output = output.substring(0, output.length - 1)
                        }
                        else {
                            output += letter
                        }
                    }
                }
                println(output)
            }
            print("heyheyhelol" + output)
            findViewById<TextView>(R.id.output).text = output
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
    val file = File("/data/user/0/com.mwanczyk.wso/files/output.txt").readText(Charsets.UTF_8)

    return file
}
