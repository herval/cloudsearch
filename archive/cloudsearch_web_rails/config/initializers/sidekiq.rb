# if Rails.env.production?
  # $redis = Redis.new(url: ENV['REDISCLOUD_URL'])
  # Sidekiq.configure_client do |config|
    # config.redis = { url: ENV['REDISCLOUD_URL'] }
  # end
  # Sidekiq.configure_server do |config|
    # config.redis = { url: ENV['REDISCLOUD_URL'] }
  # end
# else
  $redis = Redis.new(:host => 'cloudsearch_redis_1', :port => 6379)
# end
