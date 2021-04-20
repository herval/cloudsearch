import React from 'react'

const Button = ({ text, onClick }) => {
  return (
    <a onClick={onClick}>{text}</a>
  )
}

export default Button