# a facebook conversation (a grouping of individual messages)
class FacebookChat < SearchableDocument

  def path
    # TODO
    #"https://www.facebook.com/messages/?action=read&tid=id.250908971667592"
  #  "inbox"
  end

  def title
    ""
  end

  def summary
    self.body
  end

end
