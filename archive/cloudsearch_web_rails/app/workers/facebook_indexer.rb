class FacebookIndexer
  include Sidekiq::Worker
  sidekiq_options backtrace: true, retry: false, unique: true, queue: :facebook

  def perform(account_id)
    @account = ServiceAccount.find(account_id)
    @account.update_attribute(:synced_at, Time::now)
  end

end


