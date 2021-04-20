require 'sidekiq'

Sidekiq.configure_client do |config|
  config.redis = { :size => 1 }
end

require 'sidekiq/web'

Sidekiq::Web.use Rack::Auth::Basic do |username, password|
  username == 'admin' && password == 'admin'
end

run Sidekiq::Web