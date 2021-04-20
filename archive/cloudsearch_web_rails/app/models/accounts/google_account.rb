class GoogleAccount < Account

  def token_url
    "https://accounts.google.com/o/oauth2/token"
  end

  def refresh_token_params
    {
      refresh_token: self.refresh_token,
      grant_type: 'refresh_token',
      client_id: self.class.client_id,
      client_secret: self.class.client_secret
    }
  end

  def self.client_id
    ENV['GOOGLE_KEY']
  end

  def self.client_secret
    ENV['GOOGLE_SECRET']
  end

  def description
    "Google (#{self.email})"
  end

  def profile_meta_attributes
    {
      picture: self.metadata["picture"],
      path: self.metadata["link"],
      gender: self.metadata["gender"],
      locale: self.metadata["locale"]
    }
  end

  def setup_service_accounts
    [
      ServiceAccount.new(name: "Gmail", service: "gmail"),
      ServiceAccount.new(name: "Contacts", service: "contacts")
    ]
  end
end
