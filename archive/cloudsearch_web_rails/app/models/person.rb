# all representations of a person (and his many profiles) FOR A GIVEN USER
class Person < ActiveRecord::Base

  validates_presence_of :user

  belongs_to :user
  belongs_to :account

  before_save :aggregate_attributes

  has_many :profiles
  has_many :documents

  # merges profiles & destroys the other_person
  def self.merge!(other_person)
    other_person.profiles.each do |p|
      self.emails << p.email
      p.update_attributes(person: self)
    end

    other_person.destroy
    self
  end

  def self.find_or_create(filters, attrs = {})
    attrs = attrs.merge(filters)
    attrs[:emails] = [filters[:emails]] unless attrs[:emails].is_a?(Array)
    Person.where(filters).first || Person.create(attrs)
  end

  def aggregate_attributes
    self.emails = (self.profiles.collect(&:email) + self.emails).uniq
    if self.description.blank?
      self.description = self.profiles.collect(&:description).select{|p| !p.blank? }.first || self.emails.first
    end
    self.picture ||= self.profiles.collect(&:picture).compact.first
  end

end
