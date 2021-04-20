import React from 'react'
import {remote} from 'electron'
import {setReceiver} from './lib/socket'
import open from "open"
import {delayedSearch, stopSearch} from './lib/searchThrottle'
import {resize} from './lib/windowControls'
import _ from "lodash"
import Queue from 'tiny-queue'
import SearchBox from './lib/searchbox'
import Results from './lib/searchResults'
import {addKeyHandler, removeKeyHandler} from './lib/keyboard'
import Header from './lib/header'
import StatusBar from './lib/statusBar'

const info = remote.require('./lib/log').info
const event = remote.require('./lib/stats').event

const favorites = remote.require('./lib/server').favorites
const toggleFavorite = remote.require('./lib/server').toggleFavorite
const savedSearches = remote.require('./lib/server').savedSearches
const recentSearches = remote.require('./lib/server').recentSearches

const server = remote.require('./lib/server')
const listAccounts = server.listAccounts


const EXPANDED_SIZE = {width: 900, height: 655}
const DEFAULT_SIZE = EXPANDED_SIZE //{ width: 750, height: 114 }
const FILTERING_SIZE = {width: 750, height: 685}

export default class Main extends React.Component {
  constructor({history}) {
    super()
    this.state = this.emptyState()
    this.state.socket = null

    this.currentSearchId = null
    this.resultsRef = null

    this.buffer = new Queue() // items received from the websocket but not yet rendered

    this.handleSearch = this.handleSearch.bind(this)
    this.handleSocket = this.handleSocket.bind(this)
    this.handleService = this.handleService.bind(this)
    this.handleFilter = this.handleFilter.bind(this)
    this.handleOpen = this.handleOpen.bind(this)
    this.handleFavorite = this.handleFavorite.bind(this)
    this.handleHover = this.handleHover.bind(this)
  }

  handleOpen(item) {
    // console.log(item)
    if (item != null) {
      if (item.permalink != null && item.permalink.length > 0) {
        event("Action", "OpenContent")
        open(item.permalink)
      } else if (item.kind === "SearchQuery") {
        this.handleSearch(item.title)

      }
    }
  }

  handleFavorite(item) {
    if (item != null) {
      toggleFavorite(item.id).then(() => {
        let newData = this.state.data

        newData.forEach((d) => {
          if (d.id === item.id) {
            d.favorited = !d.favorited
          }
        })

        newData = newData.filter((i) => {
          // filter out non-favorited
          return (i.searchType === "favorites" && i.favorited) || (i.searchType !== "favorites")
        })

        let selected = this.state.selectedResult
        if (!newData.indexOf(this.state.selectedResult)) {
          selected = null
        }

        this.setState({
          data: newData,
          selectedResult: selected
        })
      })
    }
  }

  showingBookmarks() {
    return !this.state.searching || this.state.searchValue === ""
  }

  updateSaved() {
    Promise.all([
      favorites(),
      savedSearches(),
      recentSearches(),
    ]).then(data => {
      const [fav, sav, rec] = data

      const favorites = fav.results
      const saved = sav.results
      const recent = rec.results

      this.setState({
        data: favorites.concat(saved).concat(recent),
      })
    })
  }

  handleService() {
    this.props.history.push('/settings')
  }

  handleFilter() {
    if (this.state.filterShow === '') {
      this.setState({filterShow: 'hidden', headerFilter: 'noFilter'})
      this.state.searchValue !== '' ? resize({height: FILTERING_SIZE.height}) : resize({height: DEFAULT_SIZE.height})
    } else {
      this.setState({filterShow: '', headerFilter: ''})
      resize({height: EXPANDED_SIZE.height})
    }
  }

  handleSocket(msg) {
    // TODO dedup here as remote results take longer to arrive but are more up-to-date

    if (msg.searchId !== this.currentSearchId || this.currentSearchId == null) {
      // we don't need this result anymore
      return
    }

    if (msg.error !== undefined) {
      info(`ERROR: ${JSON.stringify(msg)}`)
      // TODO handle errors
      return
    }

    if (msg.type === "status" && msg.status === "done") {
      let kind = msg.accountType
      if (kind !== null && kind !== undefined) {
        event("SearchResults", kind, "totalResults", msg.totalResults)
      } else {
        // all done
        this.setState({searching: false})
      }
    }

    if (msg.type === "result") {
      msg.result.searchId = msg.searchId
      this.buffer.push(msg.result)
    }
  }

  handleSearch(search) {
    // console.log("SEARCHING: " + search)
    const trimmedLength = _.trim(search).length
    const minSearchSize = 2

    let newState = {}

    if (trimmedLength >= minSearchSize) {
      newState = Object.assign(newState, {
        searching: true,
        showing: false,
      })

      delayedSearch(this.state.socket, search).then((newSearchId) => {
        // info("Old search: " + this.currentSearchId + ", new search: " + newSearchId)
        this.currentSearchId = newSearchId // not using react state to avoid re-rendering
      }).catch(() => {
        // search was cancelled
      })
    }

    newState = Object.assign(newState, {
      searchValue: search,
      data: [],
      selectedResult: null,
    })

    this.setState(newState)

    if (trimmedLength >= minSearchSize) {
      this.state.filterShow === '' ? resize({height: FILTERING_SIZE.height}) : resize({height: EXPANDED_SIZE.height})
    } else {
      this.state.filterShow === '' ? resize({height: FILTERING_SIZE.height}) : resize({height: DEFAULT_SIZE.height})
    }
  }

  handleKeyUp(e) {
    if (e.keyCode == '13') { // enter
      e.preventDefault()
      // info("enter!")
      if (this.state.selectedResult !== null) {
        this.handleOpen(this.state.selectedResult)
      }
    }

    if (e.keyCode == '27') { // esc
      e.preventDefault()
      // info("esc!")
      this.escape()
    }

    if (e.keyCode == '38') { // up
      e.preventDefault()
      this.handleHover(this.prevItem(this.state.selectedResult))
    }

    if (e.keyCode == '40') { // down
      e.preventDefault()
      this.handleHover(this.nextItem(this.state.selectedResult))
    }

    if (this.state.searchValue === "" && this.state.searching) {
      this.clear()
    }
  }

  escape() {
    // if (this.state.searchValue.length == 0) {
    // info("hiding...")
    // hide()
    // } else {
    // info("resetting state")
    this.clear()
    // }
  }

  emptyState() {
    return {
      searchValue: "",
      data: [],
      selectedResult: null,
      searching: false,
      showing: true,
    }
  }

  clear() {
    this.buffer = new Queue()
    stopSearch(this.state.socket, this.currentSearchId)
    this.currentSearchId = null
    // TODO send a "cancel" to the server
    this.setState(this.emptyState())
    this.updateSaved()
    this.state.filterShow === '' ? resize({height: FILTERING_SIZE.height}) : resize({height: DEFAULT_SIZE.height})
  }

  componentWillUnmount() {
    // console.log('removing...')
    removeKeyHandler("main")
  }

  setupOrganizer() {
    setTimeout(() => {
      if (this.buffer.length > 0) {
        let previousSelectedIndex = -1
        if (this.state.selectedResult != null) {
          previousSelectedIndex = this.state.data.indexOf(this.state.selectedResult)
        }

        let data = this.state.data // _.map(this.state.data, _.clone)
        while (this.buffer.length > 0) {
          const t = this.buffer.shift()
          if (t !== null && t !== undefined) {
            data.push(t)
          }
        }
        data = _.uniqBy(data, 'id')
        data = _.sortBy(data, (d) => -d.order) // TODO sort?

        // select the first thing in the list
        let selected = this.state.selectedResult
        if (selected == null && previousSelectedIndex === -1) {
          selected = data[0]
        }

        this.setState({
          data: data,
          selectedResult: selected,
          showing: true,
        })
      }
      this.setupOrganizer()
    }, 200)
  }

  prevItem(item) {
    if (item === null) {
      return this.state.data[0]
    }

    const curr = this.state.data.indexOf(item)
    if (curr === 0) {
      return this.state.data[0]
    }

    return this.state.data[curr - 1]
  }

  nextItem(item) {
    if (item === null) {
      return this.state.data[0]
    }

    const curr = this.state.data.indexOf(item)
    if (curr >= this.state.data.length - 1) {
      return this.state.data[curr]
    }

    return this.state.data[curr + 1]
  }

  handleHover(item) {
    if (item !== this.state.selectedResult) {
      // if (this.resultsRef != null) {
      //   const res = this.resultsRef.childNodes
      //   if (res !== null && res.length > index) {
      //     // TODO scroll the pane
      //     // const currentTop = this.resultsRef.offsetTop
      //     // const currentBottom = currentTop + this.resultsRef.offsetHeight

      //     // const selected = res[index]
      //     // console.log(currentTop, currentBottom, selected.offsetTop, selected.offsetHeight)

      //     // // item is to the bottom of the view
      //     // if (currentBottom > selected.offsetTop) {
      //     //   console.log("to the bottom!")
      //     // } else if (currentTop > selected.offsetTop) {
      //     //   console.log("to the top!")
      //     // }
      //     // if scrolling up, make sure selected item is at top
      //     // if scrolling down, make sure selected item is at least on bottom
      //   }
      // }

      // TODO search

      this.setState({
        selectedResult: item,
      })
    }
  }

  componentDidMount() {
    listAccounts().then(accts => {
      if (accts.length === 0) {
        // don't show search unless there's a registered account
        this.props.history.push("/settings")
      } else {
        resize(DEFAULT_SIZE)
        this.clear()
        try {
          setReceiver(this.handleSocket).then((socket) => {
            this.setState({socket: socket})
            // TODO we can display the connection status somewhere in the UI (eg a red/green
            // dot)
          })
        } catch (error) {
          info('error connecting to socket: ' + error)
        }
        this.setupOrganizer()
        // TODO keep the focus on the search input
        addKeyHandler("main", this.handleKeyUp.bind(this))
      }
    })
  }

  setResultsRef(r) {
    this.resultsRef = r
  }

  searchPlaceholder() {
    if (this.state.selectedResult != null && this.state.selectedResult.kind === "SearchQuery") {
      return this.state.selectedResult.title
    }
    return "Search..."
  }

  render() {
    return (
      <div className="page darwin">
        <div className="container">

          <Header>
            <img width="53px" src="./img/logo.svg"/>
            <SearchBox
              searchText={this.state.searchValue}
              placeholder={this.searchPlaceholder()}
              onChange={this.handleSearch}/>
            <i
              className="fas fa-cog icons-search"
              width="21px"
              onClick={this.handleService}/>
          </Header>

          <Results
            resultsRef={(r) => this.setResultsRef(r)}
            data={this.state.data}
            searching={this.state.searching}
            showing={this.state.showing}
            searchValue={this.state.searchValue}
            handleOpen={this.handleOpen}
            handleFavorite={this.handleFavorite}
            handleHover={this.handleHover}
            selectedResult={this.state.selectedResult}/>

          <StatusBar/>
        </div>
      </div>
    )
  }
}
