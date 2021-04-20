# class CustomRemovalJob < Sunspot::Queue::Sidekiq::RemovalJob
  # sidekiq_options queue: :sunspot, retry: false

  # def perform(klass, id)
    # obj = constantize(klass).find(id) rescue nil
    # without_proxy do
      # ::Sunspot.remove_by_id(klass, id)
    # end if obj
  # end
# end
