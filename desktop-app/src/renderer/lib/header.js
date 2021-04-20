import React from 'react'

const Header = ({ children }) => {
  return (
    <div className="container-header searchbox">
      {children}
    </div>
  )
}

export default Header