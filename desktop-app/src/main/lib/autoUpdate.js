import { autoUpdater } from "electron-updater"
import { info } from "./log"

const update = () => {
  info("Auto update... eventually")
  autoUpdater.checkForUpdatesAndNotify()
}

module.exports = {
  autoUpdate: update,
}