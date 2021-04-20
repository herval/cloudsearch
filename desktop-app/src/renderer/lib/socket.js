const info = require('electron').remote.require('./lib/log').info
const remote = require('electron').remote
const websocketPath = remote.require('./lib/server').websocketPath


// returned struct client can hook up listeners to
const socket = {
	connection: null,
	onReceive: null,
	onReconnecting: null,
	onReconnected: null,
	send: msg => {
		// info(msg)
		return socket.connection.send(JSON.stringify(msg))
	},
}

const connectToServer = async () => {
	const url = websocketPath()

	const sock = new WebSocket(url)
	socket.connection = sock

	sock.onmessage = e => {
		// console.log("message received: " + e.data)
		if (socket.onReceive != null) {
			socket.onReceive(JSON.parse(e.data))
		}
	}

	sock.onclose = e => {
		info("connection closed (" + e.code + ") - reconnecting...")
		if (socket.onReconnecting != null) {
			socket.onReconnecting()
		}

		setTimeout(() => {
			Promise.resolve(connectToServer())
		}, 1000)
	}

	return sock
}

// return a socket obj
module.exports = {
	setReceiver: async (onReceive) => {
		socket.onReceive = onReceive
		return socket
	},
	connect: connectToServer,
}
