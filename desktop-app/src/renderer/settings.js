import React from 'react'
import {remote} from 'electron'
import {resize, hide} from './lib/windowControls'
import open from "open"
import StatusBar from './lib/statusBar'

const server = remote.require('./lib/server')
const getAccountAuthUrl = server.getAccountAuthUrl
const listAccounts = server.listAccounts
const removeAccount = server.removeAccount
const createBasicAccount = server.createBasicAccount
import {iconTag} from './lib/icons'
import Header from './lib/header'

const Account = ({account, name, type, icon, onDelete}) => {
  return (
    <div className="active-services">
      <div className="active-services-logo">
        {iconTag(icon)}
      </div>
      <div>{name}</div>
      <a onClick={() => onDelete(account)}>[x]</a>
    </div>
  )
}

export default class Settings extends React.Component {
  constructor({history}) {
    super()
    this.state = {
      accounts: []
    }
  }

  componentDidMount() {
    resize({width: 700, height: 600})

    this.loadAccounts()
  }

  loadAccounts() {
    listAccounts().then((accts) => {
      this.setState({accounts: accts})
      resize({width: 700, height: 600})
    })
  }

  async newAccountBasic(type) {
    this.props.history.push(
      '/account_login',
      {
        type: type
      }
    )
    // TODO
    // let u = await smalltalk.prompt("Username?", "")
    // let p = await smalltalk.prompt("Password?", "", { type: "password" })
    // let s = await smalltalk.prompt("Server?", "")

    // await createBasicAccount(type, u, p, s)
  }

  async newAccount(type) {
    let r = await getAccountAuthUrl(type)
    this.props.history.push(
      '/oauth_connect',
      {
        url: r.url
      }
    )
    // open(r.url)
  }

  handleDelete(account) {
    alert("OH NO! CONFIRM! " + account.id)
    removeAccount(account.id).then((r) => {
      alert("removed!")
      this.loadAccounts()
    })
  }

  render() {
    return (
      <div className="page darwin">
        <div className="container">
          <Header>
            <img width="53px" src="./img/logo.svg"/>
            <p>Settings</p>
          </Header>
          <div className="container-content">
            <div className="settings">
              <h1>Connect new account</h1>
              <a onClick={() => this.newAccount('Google')}>{iconTag('fab fa-google')}
                &nbsp;Google</a><br/>
              <a onClick={() => this.newAccount('Dropbox')}>{iconTag('fab fa-dropbox')}
                &nbsp;Dropbox</a><br/>
              <a onClick={() => this.newAccount('Github')}>{iconTag('fab fa-github')}
                &nbsp;Github</a><br/>
              <a onClick={() => this.newAccount('Slack')}>{iconTag('fab fa-slack')}
                &nbsp;Slack</a><br/>
              <a onClick={() => this.newAccountBasic('Confluence')}>{iconTag('fab fa-confluence')}
                &nbsp;Confluence</a><br/>
              <a onClick={() => this.newAccountBasic('Jira')}>{iconTag('fab fa-jira')}
                &nbsp;Jira</a><br/>
              <br/>
              <h1>Connected accounts</h1>
              {this.state.accounts.length == 0 ?
                <h3>Empty</h3> : null}
              {this.state.accounts.map((a) =>
                <Account key={a.id} account={a} icon={a.icon} name={a.description} type={a.type} onDelete={a => this.handleDelete(a)}/>)
              }
              <br/>
              {
                this.state.accounts.length > 0 &&
                <a onClick={() => this.props.history.push("/")}>Back</a>
              }
            </div>
            <StatusBar/>
          </div>
        </div>
      </div>
    )
  }
}
