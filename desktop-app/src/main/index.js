import { app, BrowserWindow, ipcMain, Menu, globalShortcut, Tray } from 'electron'
import path from 'path'
import url from 'url'
import { teardown, spawnOrReuse } from './lib/server'
import { info } from './lib/log'
import { autoUpdate } from './lib/autoUpdate'
import { event } from './lib/stats'
import settings from './lib/settings'

/**
 *
 * REMINDER: THIS DOES NOT RUN ON THE SAME SANDBOX AS ALL OTHER FILES IN THE PROJECT!
 *
 **/

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow = null
let tray = null

const setupMenus = () => {
  const template = [
    {
      label: "Application",
      submenu: [
        { label: "About", selector: "orderFrontStandardAboutPanel:" },
        { type: "separator" },
        { label: "Quit", accelerator: "Command+Q", click: function () { app.quit(); } }
      ]
    }, {
      label: "Edit",
      submenu: [
        { label: "Undo", accelerator: "CmdOrCtrl+Z", selector: "undo:" },
        { label: "Redo", accelerator: "Shift+CmdOrCtrl+Z", selector: "redo:" },
        { type: "separator" },
        { label: "Cut", accelerator: "CmdOrCtrl+X", selector: "cut:" },
        { label: "Copy", accelerator: "CmdOrCtrl+C", selector: "copy:" },
        { label: "Paste", accelerator: "CmdOrCtrl+V", selector: "paste:" },
        { label: "Select All", accelerator: "CmdOrCtrl+A", selector: "selectAll:" }
      ]
    }
  ]
  if (!process.env.DEVMODE) {
    Menu.setApplicationMenu(Menu.buildFromTemplate(template))
  }

  // TODO activate this with a user preference
  // if (app.dock) {
  //   app.dock.hide() // no dock icon
  // }

  tray = new Tray(path.join(__dirname, 'img', 'trayIcon.png'))
  // TODO add quit option
  // const contextMenu = Menu.buildFromTemplate([
  //   { label: 'About', type: 'radio' },
  //   { label: 'Quit', type: 'radio' },
  // ])
  // tray.setContextMenu(contextMenu)
  tray.setTitle("Loading...")
  tray.on("click", () => {
    toggleShow()
  })
}

const setupGlobalEvents = () => {
  registerAppToggle()

  ipcMain.on('hide-me', (event, arg) => {
    hide()
  })

  ipcMain.on('resize-me', (event, arg) => {
    const height = (arg.height !== null && arg.height !== undefined) ? arg.height : mainWindow.getSize()[1]
    const width = (arg.width !== null && arg.width !== undefined) ? arg.width : mainWindow.getSize()[0]
    mainWindow.setSize(width, height, true)
  })

}

const createWindow = () => {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    show: false,
    width: 700,
    height: 600,
    transparent: true,
    frame: false,
    resizable: true,
    movable: true,
    alwaysOnTop: false,
    minimizable: false,
    closable: true,
    maximizable: false,
    titleBarStyle: 'hidden',
    webPreferences: {
      devTools: process.env.DEVMODE ? true : false
    }
  })

  info("Creating main window...")
  mainWindow.loadURL(url.format({
    pathname: path.join(__dirname, '..', 'renderer', 'index.html'),
    protocol: 'file:',
    slashes: true
  }))
  mainWindow.setVisibleOnAllWorkspaces(true)

  if (process.env.DEVMODE) {
    const { default: installExtension, REACT_DEVELOPER_TOOLS } = require('electron-devtools-installer')

    installExtension(REACT_DEVELOPER_TOOLS)
      .then((name) => console.log(`Added Extension:  ${name}`))
      .catch((err) => console.log('An error occurred: ', err));

    // Open the DevTools.
    mainWindow.webContents.openDevTools()
  }

  // Emitted when the window is closed.
  mainWindow.onbeforeunload = (e) => {
    info("Window close called")
    // e.returnValue = true
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    mainWindow = null
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })
}

const registerAppToggle = () => {
  globalShortcut.unregisterAll()
  info("Registering app toggle")
  const openCloseShortcut = settings.getOrElse('openCloseShortcut', 'CommandOrControl+Shift+?')
  const globalCmd = globalShortcut.register(openCloseShortcut, () => {
    info('CommandOrControl+Shift+? is pressed')
    toggleShow()
  })

  if (!globalCmd) {
    info('Global shortcut registration failed')
  }
}

const toggleShow = () => {
  if (mainWindow.isVisible() && mainWindow.isFocused()) {
    hide()
  } else {
    show()
  }
}

const show = () => {
  info("app was invisible, showing")
  mainWindow.show()
}

const hide = () => {
  info("app was visible, hiding")
  Menu.sendActionToFirstResponder('hide:')
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', () => {
  event('App', 'Open')
  autoUpdate()
  setupMenus()
  setupGlobalEvents()
  createWindow()
  tray.setTitle("")
})

// Quit when all windows are closed.
app.on('will-quit', (e) => {
  // TODO only quit on the menu bar
  // e.preventDefault()

  event('App', 'Close')
  info("Shutting down...")
  Promise.resolve(teardown())
  globalShortcut.unregisterAll()
  app.exit()
})

// app.on('browser-window-blur', () => {
//   info("hiding....")
//   hide()
// })

// app.on('window-all-closed', (e) => {
//   info("Shutting down...")
//   Promise.resolve(teardown())
//   app.exit()
// })

app.on('activate', function () {
  // On OS X it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (mainWindow === null) {
    createWindow()
  }
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
