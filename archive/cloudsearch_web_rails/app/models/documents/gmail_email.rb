# TODO store replies inside the same document?
# TODO rename to Email
class GmailEmail < SearchableDocument
  store_accessor :metadata,
                   :message_id,
                   :subject, :to, :from, :body, :folder,
                   :cc, :reply_to, :in_reply_to, :keywords,
                   :attachments, :sent_at

  before_save :associate_profiles

  searchable do
    text :title, more_like_this: true, boost: 5.0, stored: true
    text :summary, more_like_this: true, stored: true
    text :profiles, more_like_this: true do
      self.profiles.collect(&:email)
    end
    string :person_ids, multiple: true do
      self.profiles.collect(&:person_id)
    end
    string :user_id
  end

  def path
    "inbox"
  end

  def title
    self.subject
  end

  def summary
    self.body
  end

  private

  def all_recipients
    emails = []
    emails << (self.to.is_a?(String) ? self.to : self.to.to_a)
    emails << (self.from.is_a?(String) ? self.from : self.from.to_a)
    emails << (self.cc.is_a?(String) ? self.cc : self.cc.to_a)
    emails << (self.reply_to.is_a?(String) ? self.reply_to : self.reply_to.to_a)
    emails = emails.flatten.compact

    emails.collect do |email_and_name|
      if email_and_name.index(" <")
        name, email = email_and_name.split(" <")[0..1]
        # TODO rename surrounding "" from names
        { name: name, email: email.chop! }
      else
        { email: email_and_name.gsub("<", "").gsub(">", "") }
      end
    end
  end

  def associate_profiles
    new_profiles = all_recipients.collect do |pp|
      Profile.update_or_create({ account: self.account,
                                 email: pp[:email],
                                 name: pp[:name] },
                               {  })
    end

    self.profiles = new_profiles.flatten.compact.uniq
  end

end
