import {app} from 'electron'

module.exports = {
	storagePath: () => app.getPath("userData"), //desktop"),
}