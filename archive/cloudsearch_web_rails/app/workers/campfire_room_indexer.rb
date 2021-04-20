class CampfireRoomIndexer < BaseIndexer
  sidekiq_options backtrace: true, retry: false, unique: true, queue: :campfire

  def account_type
    ThirtySevenSignalsAccount
  end

  def init_api_client
    campfire = Tinder::Campfire.new(service_account.subdomain,
                         oauth_token: service_account.account.token)
    campfire.find_room_by_id(@options[:room_id])
  end

  def iterate!
    next_day = ((service_account[:cutoff] && service_account[:cutoff][room.id]) || 
                service_account.documents.max(:date) || 
                Date.today-4.years).to_date
    room = api_client
    @profiles_by_uid = {}

    while(next_day <= Date.today)
      puts "Trying chats on #{next_day}..."
      raw_msgs = room.transcript(next_day)
      if raw_msgs.size > 0
        chat = CampfireChat.update_or_create_on(
          {
            service_account: service_account,
            date: next_day.to_time
          },
          {
            room_name: room.name,
            room_id: room.id,
            guest_url: room.guest_url
          }
        )

        raw_msgs.each do |msg|
          process(msg)
        end
      end
      service_account[:cutoff] ||= {}
      service_account[:cutoff][room.id] = next_day.to_time
      service_account.save
      next_day += 1.day
    end
  end

  def process(msg)
    user = msg.user
    profile = nil
    if user
      profile =  @profiles_by_uid[user.id] || 
        Profile.update_or_create({ service_account: service_account, 
                                   uid: user.id }, 
                                   { uid: user.id,
                                     email: user.email,
                                     picture: user.avatar_url,
                                     name: user.name })

      @profiles_by_uid[user.id] ||= profile
    end

    if msg.type == 'TextMessage' || msg.type == 'PasteMessage'
      CampfireMessage.update_or_create(body: msg.body, 
                                       room_id: msg.room.id,
                                       room_name: msg.room.name,
                                       profile: profile,
                                       created_time: msg.created_at # TODO ???
                                      )
    end
  end

  # def perform(opts)
    # while(next_day <= Date.today)
      # if msgs.size > 0
        # CampfireChat.update_or_create_on(
          # {
            # service_account: service_account,
          # },
          # {
            # profiles: profiles,
            # room_name: room.name,
            # guest_url: room.guest_url,
            # messages: msgs
          # }
        # )
      # end

    # end
  # end
end
