import React from 'react'
import {remote} from 'electron'

// const info = remote.require('./lib/log').info

const iconTag = (iconName, styles) => {
  return <i style={styles} className={iconName}/>
}

module.exports = {
  iconTag: iconTag,
}
