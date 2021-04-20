import { ipcRenderer, remote } from 'electron'


const resize = ({ width, height }) => {
  setTimeout(() => { // this makes the animation smoother lol
    ipcRenderer.send('resize-me', { width: width, height: height })
  }, 200)
}

const hide = () => {
  ipcRenderer.send('hide-me')
}

module.exports = {
  resize: resize,
  hide: hide,
}
