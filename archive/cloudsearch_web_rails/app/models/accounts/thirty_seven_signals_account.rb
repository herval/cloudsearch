class ThirtySevenSignalsAccount < Account

  def token_url
    "https://launchpad.37signals.com/authorization/token"
  end

  def refresh_token_params
    {
      refresh_token: self.refresh_token,
      type: 'refresh',
      client_id: self.class.client_id,
      client_secret: self.class.client_secret
    }
  end

  def self.client_id
    ENV['THIRTY_SEVEN_SIGNALS_KEY']
  end

  def self.client_secret
    ENV['THIRTY_SEVEN_SIGNALS_SECRET']
  end

  def description
    "37Signals (#{self.email})"
  end

  def setup_service_accounts
    self.service_accounts = self.metadata['accounts'].collect do |acct|
      subdomain = acct['href'].split(".")[0].gsub("https://", "")
      ServiceAccount.new(name: acct['name'],
                        url: acct['href'],
                        uid: acct['id'],
                        metadata: {
                          subdomain: subdomain,
                        },
                        service: acct['product'])
    end
  end

end

