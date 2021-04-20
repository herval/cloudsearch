import Analytics from 'electron-google-analytics'
import { info } from './log'
import { deviceId, appVersion } from './app'

const analytics = new Analytics('UA-112051013-1', false, appVersion)

const screenView = (page) => {
  event('ScreenView', page)
}

const event = (category, name, label, value) => {
  info(`Event: ${category} ${name}`)
  Promise.resolve(
    analytics.event(category, name, { evLabel: label, evValue: value, clientID: deviceId })
  )
}

module.exports = {
  screenView: screenView,
  event: event,
}