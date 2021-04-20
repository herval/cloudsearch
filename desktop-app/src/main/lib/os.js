import process from 'process'

module.exports = {
  isMac: () => { return /^darwin/.test(process.platform) },
  isWin: () => { return /^win/.test(process.platform) },
}
