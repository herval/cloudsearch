# a user data on a specific ACCOUNT.
# TODO make these belong to SERVICES instead (eg.: google, 37signals, etc), as users are UNIQUE in these systems
class Profile < ActiveRecord::Base
  belongs_to :account
  belongs_to :service_account
  belongs_to :person
  belongs_to :user
  has_and_belongs_to_many :documents

  validates_presence_of :account, :user

  before_validation :set_user

  def self.update_or_create(conditions, opts)
    opts.reject! { |k, v| v.blank? }

    existing = Profile.where(conditions).first
    existing && existing.update_attributes(opts)
    existing || Profile.create(opts)
  end

  #def self.find_or_create(opts)
  #  opts.reject! { |k, v| v.blank? }
  #
  #  existing = Profile.where(account: opts[:account],
  #                           email: opts[:email]).first
  #  existing && existing.update_attributes(opts)
  #  existing || Profile.create(opts)
  #end

  def description
    self.name || self.email
  end

  # updates the person on all associated docs (TBD - no need to update the docs for now)
  def update_person!(person)
    #old_person = self.person
    #if(old_person.nil? || old_person.id != person.id)
    #  self.documents.each do |d|
    #    persons = d.persons || []
    #    persons.delete(old_person) if old_person
    #    persons << person
    #    d.update_attribute(:persons, persons)
    #  end
    #end
    self.update_attribute(:person, person)
  end

  def set_user
    self.user ||= self.account.try(:user)
  end
end
