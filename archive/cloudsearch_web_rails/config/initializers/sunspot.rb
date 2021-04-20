# require "sunspot/queue/sidekiq"
# backend = Sunspot::Queue::Sidekiq::Backend.new
# Sunspot.session = Sunspot::Queue::SessionProxy.new(Sunspot.session, backend)

Sunspot.session = Sunspot::Rails.build_session
ActionController::Base.module_eval { include(Sunspot::Rails::RequestLifecycle) }

# Sunspot::Queue.configure do |config|
  # # Override default job classes
  # config.index_job   = CustomIndexJob
  # config.removal_job = CustomRemovalJob
# end

# module Sunspot::Queue::Sidekiq
  # class Backend
    # # Job needs to include Sidekiq::Worker
    # def enqueue(job, klass, id)
      # job.perform_async(klass.to_s, id.to_s)
    # end
  # end
# end
