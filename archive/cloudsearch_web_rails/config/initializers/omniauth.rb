Rails.application.config.middleware.use OmniAuth::Builder do
  provider :linkedin, ENV['LINKEDIN_KEY'], ENV['LINKEDIN_SECRET'],
           {
               scope: 'r_fullprofile r_emailaddress r_network r_contactinfo'
           }
  provider :dropbox_oauth2, ENV['DROPBOX_KEY'], ENV['DROPBOX_SECRET']
  provider :facebook, ENV['FACEBOOK_KEY'], ENV['FACEBOOK_SECRET'],
           {
               scope: 'email,read_mailbox,read_stream,user_friends,read_mailbox'
           }
  provider "37signals", ENV['THIRTY_SEVEN_SIGNALS_KEY'], ENV['THIRTY_SEVEN_SIGNALS_SECRET']
  provider :google_oauth2, ENV["GOOGLE_KEY"], ENV["GOOGLE_SECRET"],
    {
      approval_prompt: 'force',
      access_type: 'offline',
      scope: "https://mail.google.com/ userinfo.email userinfo.profile https://www.google.com/m8/feeds https://www.googleapis.com/auth/drive.metadata.readonly https://www.googleapis.com/auth/contacts.readonly https://www.googleapis.com/auth/calendar.readonly"
    }
end



OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE
