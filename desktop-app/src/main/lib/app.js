import { machineIdSync } from 'node-machine-id'

const machineId = machineIdSync({ original: true })

module.exports = {
  deviceId: machineId,
  appVersion: '0.0.8',
}