# aggregates profiles that don't have a related person
class PersonProfileAggregator
  include Sidekiq::Worker
  sidekiq_options backtrace: true, retry: false, unique: true

  def perform
    User.each do |user|
      puts "Linking anonymous profiles..."
      unlinked_profiles = user.profiles.where(person: nil)
      unlinked_profiles.each do |p|
        puts "Linking profile #{p.id.to_s}"
        person = Person.find_or_create({ user: user, emails: p.email })
        p.update_person!(person)
      end

      # TODO merge by name?

      puts "Merging duplicate persons..."
      aggregates = user.persons.select { |p| p.emails.size > 1 }
      aggregates.each do |person|
        similar_persons = person.emails.collect do |email|
          user.persons.where(emails: email)
        end.flatten.compact.uniq.reject { |p| p.id == person.id }
        puts "Merging #{similar_persons.size} persons to #{person.id.to_s}" if !similar_persons.blank?
        similar_persons.each { |p| person.merge!(p) }
      end

      user.reload
      orphans = user.persons.select { |p| p.profiles.empty? }
      puts "Deleting #{orphan.size} orphans..."
      orphans.each { |o| o.destroy }
    end
  end

end
