require 'thread/pool'

class BaseIndexer
  include Sidekiq::Worker

  attr_accessor :account, :api_client, :options, :service_account

  def perform(opts)
    @options = opts.with_indifferent_access
    @account = account_type.find(@options[:account_id])
    @service_account = ServiceAccount.find(@options[:service_account_id]) if @options[:service_account_id]
    @api_client = init_api_client
    @account.update_attribute(synced_field, Time::now)
    @account.refresh_token! if account.token_stale?

    before_process
    iterate!
  end

  def before_process
    # implement
  end

  def account_type
    # implement
  end

  def init_api_client
    # implement
  end

  def iterator
    # implement
  end

  def process(document)
    # implement
  end

  def iterate!
    iterator.each do |document|
      process(document)
    end
  end

  def synced_field
    :synced_at
  end

end
