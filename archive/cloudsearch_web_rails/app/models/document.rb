class Document < ActiveRecord::Base

  has_and_belongs_to_many :profiles
  #has_and_belongs_to_many :persons
  belongs_to :account
  belongs_to :service_account
  belongs_to :user

  before_save :set_user_id
  before_save :set_account

  def persons
    self.profiles.collect(&:person).compact
  end

  def set_account
    self.account ||= self.service_account.try(:account)
  end

  def set_user_id
    self.user ||= self.account.user
  end

  def self.create_on(conditions, attrs)
    existing = self.where(conditions).first
    existing || self.create(attrs.merge(conditions))
  end

  def self.update_or_create_on(filters, attrs)
    doc = self.where(filters).first
    doc && doc.update_attributes(attrs.merge(filters))
    doc || self.create(attrs.merge(filters))
  end
end
