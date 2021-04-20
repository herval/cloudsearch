const handlers = {} // name -> function(e)

document.onkeyup = (e) => {
  Object.values(handlers).forEach((h) => {
    if (h !== undefined) {
      h(e)
    }
  })
}

const addHandler = (name, h) => {
  handlers[name] = h
}

const removeHandler = (name) => {
  delete handlers[name]
}


module.exports = {
  addKeyHandler: addHandler,
  removeKeyHandler: removeHandler,
}