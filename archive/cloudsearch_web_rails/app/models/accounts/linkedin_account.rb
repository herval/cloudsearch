class LinkedinAccount < Account

  def token_url
    "https://www.linkedin.com/uas/oauth2/accessToken"
  end

  # TODO have no idea how to make it work here
  #def refresh_token_params
  #  {
  #      refresh_token: self.refresh_token,
  #      grant_type: 'refresh_token',
  #      client_id: self.class.client_id,
  #      client_secret: self.class.client_secret
  #  }
  #end

  def api_client
    LinkedIn::Client.new(self.class.client_id, self.class.client_secret)
  end

  def self.client_id
    ENV['LINKEDIN_KEY']
  end

  def self.client_secret
    ENV['LINKEDIN_SECRET']
  end

  def description
    "Linkedin (#{self.email})"
  end

  def profile_meta_attributes
    {
      name: "#{self.metadata['firstName']} #{self.metadata['lastName']}",
      picture: self.metadata["pictureUrl"],
      path: self.metadata['publicProfileUrl']
    }
  end
end
