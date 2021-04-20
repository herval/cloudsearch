const settings = require('electron-settings')

module.exports = {
  set: settings.set,
  get: settings.get,
  getOrElse: (name, fallback) => {
    let r = settings.get(name)
    if (r == null) {
      return fallback
    }
    return r
  }
}