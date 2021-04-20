class FacebookFriendsIndexer < BaseIndexer
  sidekiq_options backtrace: true, retry: false, unique: true, queue: :facebook

  def account_type
    FacebookAccount
  end

  def api_client
    account.api_client
  end

  def synced_field
    :friends_synced_at
  end

  def iterator
    api_client.friends
  end

  def process(f)
    puts "Fetching #{f.identifier}..."
    f = f.fetch
    Profile.update_or_create({ account: @account,
                               uid: f.identifier },
                             {
                                name: f.name,
                                username: f.username,
                                picture: f.picture,
                                path: f.link,
                                gender: f.gender,
                                locale: f.locale,
                                email: f.email
                             })
  end
end


