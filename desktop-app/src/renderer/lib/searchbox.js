require('./ext')
import React from 'react'
import _ from "lodash"
import { MentionsInput, Mention } from 'react-mentions'

const suggestions = []
// Object.values(CONTENT_TYPES).map((c) => {
//   return {
//     id: c.search,
//     display: c.display,
//   }
// })


export default class SearchBox extends React.Component {

  constructor({ searchText, placeholder, suggestions, onChange }) {
    super()
    this.input = null
  }

  setInput(s) {
    if (s != null) {
      s.wrappedInstance.refs.input.focus()
    }
  }

  handleChange(e) {
    this.props.onChange(e.target.value)
  }

  render() {
    return (
      <div className="search">
        <MentionsInput
          className="searchinput"
          value={this.props.searchText}
          singleLine={true}
          placeholder={this.props.placeholder || "Search..."}
          ref={(r) => this.setInput(r)}
          markup='@[__id__]'
          displayTransform={(id, display, type) => id}
          onChange={(e) => this.handleChange(e)}>
          <Mention
            trigger=":"
            data={suggestions}
            renderSuggestion={this.renderUserSuggestion} />
        </MentionsInput>
      </div>
    )
  }
}
