import { remote } from 'electron'
const info = remote.require('./lib/log').info
const event = remote.require('./lib/stats').event

let lastSentQuery = null
let lastTyped = null

const uuid = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
        function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8)
            return v.toString(16)
        })
}

const search = (term, socket, id) => {
    if (id === undefined) {
        id = uuid()
    }
    console.log("search: " + term + " --- " + id)
    socket.send({ op: "search", searchId: id, query: term })
    return id
}

const stopSearch = (socket, id) => {
    if (socket != null) {
        socket.send({ op: "cancel", searchId: id })
    }
}

// resolve to a search id, if it ever happens
const delayedSearch = (socket, query) => {
    return new Promise((resolve, reject) => {
        if (socket == null) {
            info("Socket not connected, dropping query")
            lastSentQuery = query
            return
        }

        // info("delaying search for " + query)
        if (query != lastTyped) { // user is typing
            event("Action", "Search", "characters", query.length)

            // fire cache query right away, delay live query
            lastTyped = query
            lastSentQuery = query
            let searchId = undefined

            setTimeout(() => {
                // if user kept tying, cancel the cached search
                if (query == lastTyped) {
                    searchId = search(query, socket)
                    resolve(searchId)
                } else {
                    reject()
                }
            }, 500)

            // setTimeout(() => {
            //     // if user started typing again, cancel the live search
            //     if (query == lastTyped) {
            //         search(query + " mode:Live", socket, searchId)
            //     } else {
            //         // no biggie
            //     }
            // }, 1200) // put some delay to avoid over-sending
        }
    })
}


module.exports = {
    delayedSearch: delayedSearch,
    stopSearch: stopSearch,
}
