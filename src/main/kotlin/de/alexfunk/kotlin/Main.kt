package de.alexfunk.kotlin

import io.javalin.Javalin
import io.javalin.websocket.WsContext
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import j2html.TagCreator.*
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Server

private val userUsernameMap = ConcurrentHashMap<WsContext, String>()
private var nextUserNumber = 2// Assign to username for next connecting user


fun main() {
	val port = 7070
	Javalin.create { config ->
		config.server {
			Server().apply {
				connectors = arrayOf(ServerConnector(this).apply {
					this.host = "0.0.0.0" // ip
					this.port = port // port
				})
			}

		}
		config.addStaticFiles("/public")
	}.apply {
		ws("/chat") { ws ->
			ws.onConnect { ctx ->
				val username = "User" + nextUserNumber++
				userUsernameMap.put(ctx, username)
				broadcastMessage("Server", "$username joined the chat")
			}
			ws.onClose { ctx ->
				val username = userUsernameMap[ctx]
				userUsernameMap.remove(ctx)
				broadcastMessage("Server", "$username left the chat")
			}
			ws.onMessage { ctx ->
				broadcastMessage(userUsernameMap[ctx]!!, ctx.message())
			}
		}
	}.start(port)
}

// Sends a message from one user to all users, along with a list of current usernames
fun broadcastMessage(sender: String, message: String) {
	userUsernameMap.keys.filter { it.session.isOpen }.forEach { session ->
		session.send(
			JSONObject()
				.put("userMessage", createHtmlMessageFromSender(sender, message))
				.put("userlist", userUsernameMap.values).toString()
		)
	}
}

// Builds a HTML element with a sender-name, a message, and a timestamp,
private fun createHtmlMessageFromSender(sender: String, message: String): String {
	return article(
		i("$sender says:"),
		span(attrs(".timestamp"), SimpleDateFormat("HH:mm:ss").format(Date())),
		p(message)
	).render()
}