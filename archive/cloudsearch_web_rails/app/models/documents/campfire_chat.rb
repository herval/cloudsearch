# TODO schedule to run daily
# one chat thingie per day
class CampfireChat < SearchableDocument
  store_accessor :metadata, :room_name, :guest_url, :messages, :date

  searchable do
    text :title, more_like_this: true, stored: true
    text :summary, more_like_this: true, boost: 3.0, stored: true
    text :profiles, more_like_this: true do
      self.profiles.collect(&:email).compact
    end
    string :person_ids, multiple: true do
      self.profiles.collect(&:person_id)
    end
    string :user_id
  end

  def title
    self.room_name
  end

  def summary
    self.messages.join("\n")
  end
end
