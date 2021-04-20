class GmailContactsIndexer < BaseIndexer
  sidekiq_options backtrace: true, retry: 1, unique: true, queue: :gmail

  def account_type
    GoogleAccount
  end

  def init_api_client
    GmailContacts::Google.new(account.token)
  end

  def process(contact)
    persons = contact.emails.collect { |email| Person.where(emails: email) }.flatten.compact.uniq
    persons = [Person.create(user: account.user, emails: contact.emails)] if persons.empty? # no person with ANY of the emails yet!

    if persons.size > 1
      final_person = persons.first
      persons[1..-1].each do |dup_person|
        final_person.merge!(dup_person)
      end
    end
    person = persons.first

    contact.emails.collect do |email|
      Profile.update_or_create({
                                   account: account,
                                   email: email },
                               {
                                   name: contact.name,
                                   username: contact.username,
                                   person: person
                               })
    end
  end

  def iterator
    api_client.contacts
  end
end