class User < ActiveRecord::Base
  devise :database_authenticatable, :registerable,
         :recoverable, :rememberable, :trackable, :validatable, :token_authenticatable

  has_many :accounts, dependent: :destroy
  has_many :profiles
  has_many :persons

  before_save :ensure_authentication_token

end
