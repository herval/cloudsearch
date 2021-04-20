

function updateIndicator(callback) {
  // Update the online status icon based on connectivity
  window.addEventListener('online', () => {
    callback(navigator.onLine)
  })
  window.addEventListener('offline', () => {
    callback(navigator.onLine)
  })
}

module.exports = {
  onNetworkChanged: updateIndicator,
}