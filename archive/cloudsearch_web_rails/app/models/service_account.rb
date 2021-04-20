# a 'child' account, without auth capabilities by itself
class ServiceAccount < ActiveRecord::Base
  belongs_to :account
  has_many :documents
end
