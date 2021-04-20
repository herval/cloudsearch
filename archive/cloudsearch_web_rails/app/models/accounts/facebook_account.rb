class FacebookAccount < Account
  def self.client_id
    ENV['FACEBOOK_KEY']
  end

  def self.client_secret
    ENV['FACEBOOK_SECRET']
  end

  #def token_url
  #  "https://accounts.google.com/o/oauth2/token"
  #end
  #
  #def refresh_token_params
  #  {
  #      refresh_token: self.refresh_token,
  #      grant_type: 'refresh_token',
  #      client_id: self.class.client_id,
  #      client_secret: self.class.client_secret
  #  }
  #end

  def api_client
    FbGraph::User.me(self.token)
  end

  def description
    "Facebook (#{self.email})"
  end

  def profile_meta_attributes
    {
      username: self.metadata["username"],
      picture: "https://graph.facebook.com/#{self.metadata["username"]}/picture",
      path: self.metadata["link"],
      gender: self.metadata["gender"],
      locale: self.metadata["locale"]
    }
  end
end
