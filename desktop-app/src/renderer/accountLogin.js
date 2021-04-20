import Button from "./lib/button";
import { Circle } from "better-react-spinkit";
import React from "react";
import { iconTag } from "./lib/icons";
import { remote } from "electron";
import { resize } from "./lib/windowControls";
const createBasicAccount = remote.require("./lib/server").createBasicAccount;

class AccountLogin extends React.Component {
  constructor({ history, match, location }) {
    super();
    this.state = {
      type: location.state.type,
      connecting: false,
    };
    this.handleInputChange = this.handleInputChange.bind(this);
  }

  componentDidMount() {
    resize({ width: 600, height: 800 });
  }

  closed() {
    this.props.history.goBack();
  }

  handleInputChange(event) {
    const target = event.target;
    const value = target.type === "checkbox" ? target.checked : target.value;
    const name = target.name;

    this.setState({
      [name]: value,
    });
  }

  save() {
    this.setState({
      connecting: true,
    });

    // TODO validate before submitting

    createBasicAccount(
      this.state.type,
      this.state.username,
      this.state.password,
      this.state.server
    )
      .then((r) => {
        if (r.saved) {
          this.closed();
        } else {
          // TODO notify saving failed
          this.setState({
            connecting: false,
          });
        }
      })
      .catch((err) => {
        console.log(err);
        this.setState({
          connecting: false,
        });
      });
  }

  render() {
    return (
      <div className="login-page">
        <div className="header">
          {iconTag(this.state.icon)}
          Connect Account
        </div>

        <div>
          Username
          <input
            type="text"
            name="username"
            onChange={this.handleInputChange}
          />
        </div>

        <div>
          Password
          <input
            type="password"
            name="password"
            onChange={this.handleInputChange}
          />
          <small>
            Your password will <b>not</b> be stored.
          </small>
        </div>

        <div>
          API Server
          <input type="text" name="server" onChange={this.handleInputChange} />
        </div>

        <div>
          {this.state.connecting && <Circle size={50} color="#47CDC5" />}

          <Button text="Save" onClick={() => this.save()} />
          <Button text="Cancel" onClick={() => this.closed()} />
        </div>
      </div>
    );
  }
}

export default AccountLogin;
