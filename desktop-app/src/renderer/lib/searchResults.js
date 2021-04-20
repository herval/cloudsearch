import React from 'react'
import {remote} from 'electron'
import {Circle} from 'better-react-spinkit'
import {iconTag} from './icons'
import moment from "moment"
import Preview from "./contentPreview"
import _ from "lodash"
// const info = remote.require('./lib/log').info
// const event = remote.require('./lib/stats').event

const LoadingResults = () => {
  return (
    <div className="loading">
      <center>
        <Circle size={50} color="#47CDC5"/>
      </center>
    </div>
  )
}


const EmptyResultsState = () => {
  return (
    <div className="loading">
      <p>
          <span className="noQuery">

            Refine your search with operators like service:google or type:file. Hint: type ":" for suggestions.

          </span>
      </p>
    </div>
  )
}


const NoResults = () => {
  return (
    <div className="loading">
      <p>
        <span className="noQuery">No results found :(</span>
      </p>
    </div>
  )
}

const SearchResult = ({handleOpen, handleHover, item, selected}) => {
  let permalink = item.permalink
  let snippet = item.snippet
  if (snippet === null || snippet === undefined) {
    snippet = ""
  } else {
    snippet = snippet.replaceAll("<br>", " ").substring(0, 180)
    if (snippet.length > 0) {
      snippet += '...'
    }
  }
  let timeAgo = item.timestampMillis < 0 ? null : moment(item.timestamp).fromNow()

  let icon = item.icon
  let service = item.sourceIcon
  let title = item.title
  if (item.title.length > 40) {
    title = title.substring(0, 40) + '...'
  }

  return (
    <div onClick={() => handleOpen(item)}
         onMouseOver={() => handleHover(item)}
         className={selected ? "box box-selected" : "box"}>
      <div className="box-icons">
        {iconTag(icon, {color: '#47CDC5', marginRight: '10px'})}
      </div>

      <div className="box-title">
        <span dangerouslySetInnerHTML={{__html: title}}/>
        {(timeAgo != null || snippet !== "") &&
        <div className="snippet">
          {timeAgo && <span className="timeAgo">{timeAgo}</span>}
          <span dangerouslySetInnerHTML={{__html: snippet}}/>
        </div>
        }
      </div>

      <div className="box-icons">
        <div className="typeSnippet">
          {iconTag(service)}
        </div>
      </div>
    </div>
  )
}

const titleFor = (t, items) => {
  switch (t) {
    case "search_results":
      return `Found ${items.length} results`
    case "favorites":
      return "Pinned Content"
    case "saved_searches":
      return "Saved Searches"
    case "recent_searches":
      return "Recent Searches"
  }
}

const ResultSection = ({title, children}) => {
  return (
    <div>
      <div className="resultSectionHeader">
        <span className="resultSectionTitle">{title}</span>
      </div>
      {children}
    </div>
  )
}

const Results = ({data, handleOpen, handleFavorite, selectedResult, handleHover, searching, showing, searchValue, resultsRef}) => {

  // one list per section
  const grouped = _.groupBy(data, (i) => i.searchType)
  const lists = Object.keys(grouped).map((group, j) =>  // functional brogramming bro // G: LOLz
    (
      <ResultSection title={titleFor(group, grouped[group])} key={j}>
        {
          grouped[group].map((item, i) =>
            <SearchResult handleOpen={() => handleOpen(item)} handleHover={() => handleHover(item)} key={i}
                          item={item} selected={selectedResult == item}/>
          )
        }
      </ResultSection>
    )
  )

  return (
    <div className="container-content">
      {(showing) &&
      <div className="results-container">
        <div className="results" ref={resultsRef}>
          {lists}
        </div>
        {
          selectedResult &&
          selectedResult.previewable &&
          <Preview
            selectedData={selectedResult}
            handleFavorite={handleFavorite}
            handleOpen={handleOpen}/> || null
        }
      </div>
      }

      {(searching && !showing) && <LoadingResults/>}

      {(!searching && !showing && searchValue.length == 0) && <EmptyResultsState/>}

      {(!searching && !showing && searchValue.length > 0) && <NoResults/>}
    </div>
  )
}

export default Results