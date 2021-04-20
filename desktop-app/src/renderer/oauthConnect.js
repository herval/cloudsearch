import React from 'react'
import {resize} from './lib/windowControls'

class OauthConnect extends React.Component {
  constructor({history, match, location}) {
    super()
    this.state = {
      oauthUrl: location.state.url,
      sessionId: Date.now().toString(), // change the session every time we load the page to avoid caching passwords (eg if user has multiple google accts)
    }
    this.webview = null
  }

  componentDidMount() {
    resize({width: 600, height: 800})
  }

  setWebview(wv) {
    this.webview = wv

    if (wv !== null) {
      this.webview.addEventListener('did-navigate', (event) => {
        // console.log(event)
        if(event.url.indexOf("http://localhost") === 0 && event.url.indexOf("accounts/oauth/callback") > 0) {
          // close after authenticating
          this.props.history.push("/settings")
        }
      })
    }
  }

  render() {
    return (
      <div className="webview-page">
        <webview
          className="webview-pane"
          ref={(wv) => { this.setWebview(wv) }}
          partition={this.state.sessionId}
          src={this.state.oauthUrl} />
        <div className="status-bar">
          <a onClick={() => this.props.history.goBack()}>Cancel</a>
        </div>
      </div>
    )
  }
}


export default OauthConnect