import React from 'react'
import _ from "lodash"
import {iconTag} from './icons'
import moment from 'moment'

const withBrs = (text) => {
  return text.replaceAll("\n", "<br/>")
}

const bytesToSize = (bytes) => {
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  if (bytes === 0) 
    return 'n/a'
  const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10)
  if (i === 0) 
    return `${bytes} ${sizes[i]})`
  return `${ (bytes / (1024 ** i)).toFixed(1)} ${sizes[i]}`
}

const dataRows = (title, data) => {
  if (data === null || data === undefined) {
    return
  }
  return (
    <div className="contactDetails">
      {data.map((d) => <div className="contactDetail">
        <div className="contactTitle">{title}</div>
        <div className="value">{d}</div>
      </div>)}
    </div>
  )
}

const Contact = ({data}) => {
  return (
    <div className="content-general">
      <div className="content-child">
        <Thumbnail data={data}/>
      </div>
      <div className="content-meta content-child">
        <h1>{data.details.name}</h1>
        {dataRows("", data.details.companies)}
        {dataRows("email", data.details.emails)}
        {dataRows("phone", data.details.phones)}
        {dataRows("company", data.details.companies)}
        {dataRows("address", data.details.addresses)}
        {dataRows("birthday", [data.details.birthday])}
      </div>
    </div>
  )
}

const Chat = ({data}) => {
  return (
    <div className="content-general">
      <div className="content-child">
        <Thumbnail data={data}/>
      </div>
      <div className="content-meta content-child">
        {data.timestampMillis > 0 && <span>Date:
          <b>{moment(data.timestampMillis).format('MMMM Do YYYY, h:mm:ss a')}</b><br/></span>
}
        <span
          className="content-email-body"
          dangerouslySetInnerHTML={{
          __html: withBrs(data.details.body || "")
        }}/>
      </div>
    </div>
  )
}

const Email = ({data}) => {
  return (
    <div className="email-container">
      <span className="content-email-header">
        From:
        <b>{data.details.from}</b><br/>
        To:
        <b>{data.details.to}</b><br/>
        Subject:
        <b>{data.details.subject}</b><br/>
        Sent at:
        <b>{data.timestamp}</b>
      </span>
      <hr/>
      <span
        className="content-email-body"
        dangerouslySetInnerHTML={{
        __html: withBrs(data.details.body)
      }}/>
    </div>
  )
}

const Thumbnail = ({data}) => {
  return (
    <span>
    {(data.thumbnail && data.thumbnail.length > 0) ? <img src={data.thumbnail}/> :
      <span className="no-preview">
              {iconTag(data.icon)}
            </span>}
    </span>
  )
}

const snippetFor = (data) => {
  if (data.kind === "Email") {
    return <Email data={data}/>
  } else if (data.kind === "Contact") {
    return <Contact data={data}/>
  } else if (data.kind === "Message") {
    return <Chat data={data}/>
  } else {
    return (
      <div className="content-general">
        <div className="content-child">
          <Thumbnail data={data}/>
        </div>
        <div className="content-meta content-child">
          {data.details.path &&
          <span><b>{data.details.path}</b><br/></span>
          }
          {data.timestampMillis > 0 &&
          <span>Date: <b>{moment(data.timestampMillis).format('MMMM Do YYYY, h:mm:ss a')}</b><br/></span>
          }
          {data.details.sizeBytes === undefined ? null :
            <span>Size: <b>{bytesToSize(data.details.sizeBytes)}</b><br/></span>}
        </div>
      </div>
    )
  }
}

const Preview = ({selectedData, handleFavorite, handleOpen}) => {
  // console.log(selectedData)

  return (
    <div className="preview">

      <div className="content">
        {snippetFor(selectedData)}
      </div>

      <div className="actions">

        <a onClick={() => handleOpen(selectedData)}>
          <i className="fas fa-external-link-alt actions-icon" width="14px"/>
          <span className="actions-text">OPEN</span>
        </a>

        <a onClick={() => handleOpen(selectedData)}>
          <i className="fas fa-copy actions-icon" width="14px"/>
          <span className="actions-text">COPY</span>
        </a>

        <a onClick={() => handleFavorite(selectedData)}>
          <i className={selectedData.favorited ? " fas fa-star actions-icon" : "far fa-star actions-icon"}
             width="14px"/>
          <span className="actions-text">{selectedData.favorited ? "UNSTAR" : "STAR"}</span>
        </a>
      </div>


    </div>
  )
}

export default Preview