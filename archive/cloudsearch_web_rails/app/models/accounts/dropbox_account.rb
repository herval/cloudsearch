class DropboxAccount < Account

  def self.client_id
    ENV['DROPBOX_KEY']
  end

  def self.client_secret
    ENV['DROPBOX_SECRET']
  end

  def api_client
    session = DropboxSession.new(DropboxAccount.client_id, DropboxAccount.client_secret)
    session.set_access_token(self.token, self.secret)
    DropboxClient.new(session)
  end

  def description
    "Dropbox (#{self.email})"
  end

  def profile_meta_attributes
    {
      country: self.metadata['country'],
      referral_link: self.metadata['referral_link']
    }
  end
end

