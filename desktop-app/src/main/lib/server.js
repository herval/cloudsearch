const { spawn, exec, fork } = require("child_process");

import { exceptionLoggerKey, info } from "./log";

import { spawnServerCmd } from "./spawn";

const request = require("request-promise-native");

const serverBase = "http://127.0.0.1:";
const routes = {
  healthUrl: "/health",
  listAccounts: "/accounts/list",
  createAccount: "/accounts/create",
  createBasicAccount: "/accounts/basic_auth/create",
  removeAccount: "/accounts/remove",
  authUrl: "/accounts/auth_url",
  favoriteUrl: "/contents/favorite",
  favoritesUrl: "/contents/favorites",
  savedSearchesUrl: "/searches/saved",
  recentSearchesUrl: "/searches/recent",
  killUrl: "/kill",
  searchSocketUrl: "/socket/search",
};

const newPort = async () => {
  // if (process.env.DEVMODE) {
  // use the same port to make debugging easier
  // we need to keep the port fixed due to oauth callbacks :(
  return server.port;
  // } else {
  //   return getPort()
  // }
};

const pathFor = (route) => {
  return serverBase + server.port + route;
};

// hackety-hack to support tearing down
const server = {
  pid: null, // this will be set on spawn
  proc: null, // this will be set on spawn
  port: 65432, // this will change on spawn
};

const websocketPath = () =>
  "ws://127.0.0.1:" + server.port + routes.searchSocketUrl;

async function close() {
  if (process.env.DEVMODE) {
    info("Keeping the server up in devmode");
    return Promise.resolve();
  } else {
    return fetch(pathFor(routes.killUrl));
  }
}

async function fetch(url, params) {
  let opts = {
    uri: url,
    json: true,
  };

  if (params !== undefined) {
    opts = Object.assign(opts, {
      qs: params,
    });
  }

  return request(opts);
}

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function spawnNew(port) {
  // info("server not up - bringing it... ")
  let cmd = spawnServerCmd(port);
  info("Initializing agent: " + cmd);
  const proc = spawn(cmd[0], cmd[1]);

  info("New PID: " + proc.pid);
  server.pid = proc.pid;
  server.proc = proc;

  let rejected = false;

  proc.stdout.on("data", (data) => {
    info("> " + data);
  });

  proc.stderr.on("data", (data) => {
    info("err: " + data);
    // rejected = true
    // reject(this)
  });

  proc.on("close", (code) => {
    info("child process exited with code " + code);
    rejected = true;
  });

  await delay(3000);

  info("DONE TIMEOUTNG");
  if (!rejected && server.pid != null) {
    return server;
  } else {
    throw new Error("couldn't load!");
  }
}

// favorite *or* unfavorite
async function toggleFavorite(contentId) {
  return fetch(pathFor(routes.favoriteUrl + "/" + contentId));
}

async function listAccounts() {
  return fetch(pathFor(routes.listAccounts));
}

async function createAccount(service, token) {
  return fetch(pathFor(routes.createAccount), {
    service: service,
    token: token,
  });
}

async function removeAccount(id) {
  return fetch(pathFor(routes.removeAccount + "/" + id), {});
}

async function getAccountAuthUrl(service) {
  return fetch(pathFor(routes.authUrl), { service: service });
}

async function favorites() {
  return fetch(pathFor(routes.favoritesUrl));
}

async function savedSearches() {
  return fetch(pathFor(routes.savedSearchesUrl));
}

async function recentSearches() {
  return fetch(pathFor(routes.recentSearchesUrl));
}

async function createBasicAccount(service, username, password, server) {
  return fetch(pathFor(routes.createBasicAccount), {
    service: service,
    username: username,
    password: password,
    server: server,
  });
}

let spawningPromise = null;

async function spawnOrReuse() {
  // check if proc is already running and just return it
  const port = await newPort();
  info(`Binding server on port ${port}`);

  try {
    const health = await fetch(pathFor(routes.healthUrl));
    const pid = health.pid;
    info(`EXISTING PID: ${pid}`);
    server.pid = pid;
    return server;
  } catch (e) {
    info("can't parse pid, will respawn");
    if (spawningPromise == null) {
      spawningPromise = spawnNew(port);
    }
    // spawn a new server
    return spawningPromise.then((r) => {
      // done spawning, cleanup the promise reference
      spawningPromise = null;
      return r;
    });
  }
}

async function teardown() {
  info("killing..." + server.pid);

  if (server.pid != null) {
    info("shutting down agent");

    await close();
    info("server done!");
  } else {
    info("COULDNT KILL SERVER!");
  }
}

module.exports = {
  teardown: teardown,
  spawnOrReuse: spawnOrReuse,
  listAccounts: listAccounts,
  createAccount: createAccount,
  createBasicAccount: createBasicAccount,
  removeAccount: removeAccount,
  getAccountAuthUrl: getAccountAuthUrl,
  favorites: favorites,
  savedSearches: savedSearches,
  recentSearches: recentSearches,
  toggleFavorite: toggleFavorite,
  websocketPath: websocketPath,
};
