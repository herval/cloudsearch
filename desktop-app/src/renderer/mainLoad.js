import { MemoryRouter, Route, Switch } from "react-router";

import AccountLogin from "./accountLogin";
import Loader from "./lib/loader";
import Main from "./main";
import OauthConnect from "./oauthConnect";
import React from "react";
import ReactDOM from "react-dom";
import Settings from "./settings";
import createHistory from "history/createBrowserHistory";
import { remote } from "electron";
const info = remote.require("./lib/log").info;
const screenView = remote.require("./lib/stats").screenView;
require("./lib/ext.js"); // magic

const Analytics = ({ location }) => {
  screenView(location.pathname);
  return <div></div>;
};

window.onload = () => {
  ReactDOM.render(
    <Loader>
      <MemoryRouter>
        <div>
          <Route component={Analytics} />
          <Switch>
            <Route path="/settings" component={Settings} exact />
            <Route path="/oauth_connect" component={OauthConnect} />
            <Route path="/account_login" component={AccountLogin} />
            <Route path="/" component={Main} exact />
          </Switch>
        </div>
      </MemoryRouter>
    </Loader>,
    document.getElementById("app")
  );
};
