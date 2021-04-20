class CampfireIndexer < BaseIndexer
  sidekiq_options backtrace: true, retry: false, unique: true, queue: :campfire

  def account_type
    ThirtySevenSignalsAccount
  end

  def init_api_client
    Tinder::Campfire.new(service_account.subdomain,
                         oauth_token: service_account.account.token)
  end

  def iterator
    api_client.rooms
  end

  def process(room)
    CampfireRoomIndexer.perform_async({ account_id: account.id,
                                        service_account_id: service_account.id,
                                        room_id: room.id })
  end
end


