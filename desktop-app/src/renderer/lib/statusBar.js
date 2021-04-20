import React from 'react'
import {iconTag} from './icons'
import {onNetworkChanged} from './connection'


const StatusBar = ({connected}) => {
  return (
    <span>
      {!connected &&
      <div className="status-bar">
        <span className="warning">{iconTag("fas fa-wifi")}</span>
      </div>
      }
      </span>
  )
}

const withConnection = (Component) => {
  return class extends React.Component {
    constructor(props) {
      super()
      this.state = {
        connected: true,
      }
    }

    componentDidMount() {
      onNetworkChanged((e) => {
        this.setState({
          connected: e
        })
      })
    }

    render() {
      return (
        <Component connected={this.state.connected}/>
      )
    }
  }
}


export default withConnection(StatusBar)