import {isWin, isMac} from './os'
import {app} from 'electron'
import path from 'path'
import {info, exceptionLoggerKey} from './log'
import fs from 'fs'
import {storagePath} from './paths'

const spawn = (port) => {
  const basepath = path.dirname(app.getAppPath())

  let binPath = path.join(basepath, 'cloudsearch') // under /cloudsearch on the bundled app
  if (!fs.existsSync(binPath)) {
    info("Daemon not found, likely dev mode, searching locally " + binPath + " - " + __dirname)
    binPath = path.join(__dirname, "..", "..", "..", "bin", "cloudsearch")
    info("New binpath: " + binPath)
  } else {
    info("Daemon found at " + binPath)
  }

  let cmd = null
  if (isWin()) {
    cmd = basepath + binPath + ".exe"
  } else if (isMac()) {
    cmd = binPath
  } else {
    cmd = binPath + ".bin"
  }

  let args = [
    "-httpPort", ":" + port,
    "-storagePath", storagePath(),
    "server"
  ]

  // "        'SENTRY_DSN': exceptionLoggerKey,\n" +
  // "        'SENTRY_SAMPLE_RATE': 0.7,\n" +
  // "    "

  return [cmd, args]
}

module.exports = {
  spawnServerCmd: spawn
}