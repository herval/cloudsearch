require 'thread/pool'

class FacebookChatIndexer
  include Sidekiq::Worker
  sidekiq_options backtrace: true, retry: false, unique: true, queue: :facebook

  def perform(account_id)
    @account = FacebookAccount.find(account_id)
    @account.update_attributes(chats_synced_at: Time::now)
    @account.chat_cutoff_time ||= {}
    @account.past_messages_imported ||= {}
    client = @account.api_client

    pool = Thread.pool(10)

    chats = client.inbox
    while chats
      chats.each do |room|
        pool.process do
          fetch_chat(room)
        end
      end
      chats = chats.next
    end
  end

  def fetch_chat(room)
    room_id = room.identifier
    latest_message_time = room.messages[0].created_time
    old_imported = @account.past_messages_imported[room_id]

    if (!old_imported || latest_message_time != @account.chat_cutoff_time[room_id])
      @account.chat_cutoff_time[room_id] = latest_message_time
      @account.save

      messages = room.messages
      while messages

        oldest_message_time = nil
        messages.each do |m|
          puts m.message
          FacebookMessage.create_on(
              {account: @account, uid: m.identifier},
              {
                  body: m.message,
                  from: m.from.raw_attributes,
                  chat_id: room_id,
                  created_time: m.created_time
              })
          oldest_message_time = m.created_time
        end

        if old_imported && oldest_message_time <= latest_message_time
          # don't fetch any more pages
          puts "Reached the cutoff for #{room_id}"
          messages = nil
        else
          messages = messages.next
        end
      end

      @account.past_messages_imported[room_id] = true
      @account.save
    else
      puts "No new messages on chat #{room_id}"
    end
  end

end


