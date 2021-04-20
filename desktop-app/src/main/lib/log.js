import Raven from "raven";
import { app } from "electron";
import { isWin } from "./os";
const path = require("path");
const fs = require("fs");
const log = require("electron-logger");
const paths = require("./paths");

const apiKey =
  "https://0a046ba488324eb28df53ca5fb73bf72:7a9787cc6db942bd9c36df7eeb2668f3@sentry.io/267154";
Raven.config(apiKey).install();

try {
  fs.mkdirSync(paths.storagePath());
} catch (e) {
  // ignore.
}

const f = path.join(paths.storagePath(), "out.log");

log.setOutput({
  file: f,
});

function info(msg) {
  let m = msg;
  if (isWin()) {
    m += "\r"; // lol
  }
  console.log(m);
  log.info(m);
}

module.exports = {
  info: info,
  exceptionLoggerKey: apiKey,
};
