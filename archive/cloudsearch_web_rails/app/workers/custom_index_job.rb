# class CustomIndexJob < Sunspot::Queue::Sidekiq::IndexJob
  # sidekiq_options queue: :sunspot, retry: false, unique: true, unique_job_expiration: 10

  # def perform(klass, id)
    # obj = constantize(klass).find(id) rescue nil
    # obj && obj.solr_index
  # end
# end
