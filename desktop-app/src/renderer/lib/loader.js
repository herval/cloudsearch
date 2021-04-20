import { DoubleBounce } from "better-react-spinkit";
import React from "react";
import { remote } from "electron";
import { resize } from "./windowControls";
const server = remote.require("./../main/lib/server");
const spawnOrReuse = server.spawnOrReuse;
const connectSocket = require("./socket").connect;
const info = remote.require("./../main/lib/log").info;

const connect = async () => {
  let s = await spawnOrReuse();
  // info("server up")
  let socket = await connectSocket();
  info(`Socket connected: ${JSON.stringify(socket)}`);
  return socket;
};

// load dependencies showing a spinner or something
class Loader extends React.Component {
  constructor(props) {
    super();
    this.state = {
      socket: null,
      App: props.children,
    };
  }

  componentDidMount() {
    resize({ width: 400, height: 400 });
    // setTimeout(() => {
    connect().then((s) => {
      this.setState({
        socket: s,
      });
    });
    // }, 2000)
  }

  loading() {
    return (
      <div>
        <DoubleBounce size={50} color="#47CDC5" />
        <br />
        Loading...
      </div>
    );
  }

  render() {
    return (
      <div id="loadingPage">
        {this.state.socket == null && this.loading()}
        {this.state.socket != null && this.state.App}
      </div>
    );
  }
}

export default Loader;
