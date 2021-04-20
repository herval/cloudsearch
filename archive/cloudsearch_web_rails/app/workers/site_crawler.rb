class SiteCrawler
  include Sidekiq::Worker
  #sidekiq_options queue: :crawlers, backtrace: true, retry: false

  def perform(account_id)
    account = Account.find(account_id)
    account.update_attribute(:synced_at, Time::now)

    # TODO crawl!
  end

end

