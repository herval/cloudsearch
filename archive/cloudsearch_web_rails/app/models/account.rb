class Account < ActiveRecord::Base
  validates :service, :user_id, :email, presence: true
  validates_uniqueness_of :service, scope: [:user_id, :email]

  belongs_to :user
  belongs_to :profile
  has_many :documents, dependent: :destroy
  has_many :profiles, dependent: :destroy
  has_many :service_accounts, dependent: :destroy

  after_create :associate_profile
  before_create :build_service_accounts

  scope :active, -> { where(state: "active") }

  def self.available_services
    [
        ["Dropbox",    "/auth/dropbox_oauth2",  "dropbox-account"],
        # ["37signals",  "/auth/37signals",       "thirty-seven-signals-account"],
        # ["Facebook",   "/auth/facebook",        "facebook-account"],
        ["Google",     "/auth/google_oauth2",   "google-account"],
        # ["Linkedin",   "/auth/linkedin",        "linkedin-account"]
    ]
  end

  def pending_sync?
    self.synced_at.nil? || self.synced_at < Time::now - sync_every
  end

  def sync_every
    1.hour
  end

  def disable!
    self.update_attributes(
        state: "removed"
    )
  end


  def remove_documents
    documents.destroy_all
  end

  def token_url
    nil # for refresh token purposes
  end

  def refresh_token_params
    nil # implement on subclasses
  end

  def build_service_accounts
    self.service_accounts = setup_service_accounts
  end

  def setup_service_accounts
    # implement as needed
    []
  end

  def token_stale?
    !self.token_expires_at.nil? && self.token_expires_at < Time::now
  end

  def refresh_token!
    new_token = HTTParty.post(token_url, body: refresh_token_params)

    self.update_attributes(
        token: new_token.parsed_response['access_token'],
        token_expires_at: (new_token.parsed_response['expires_in'] && (Time::now + new_token.parsed_response['expires_in'])) || nil
    )
  end

  def profile_meta_attributes
    {}
  end

  def associate_profile
    attrs = {account: self,
             email: self.email,
             uid: self.uid,
             name: self.name,
             metadata: profile_meta_attributes
    }
    profile = Profile.update_or_create({account: self,
                                        email: self.email},
                                       attrs)
    self.update_attributes(profile: profile)
  end
end
