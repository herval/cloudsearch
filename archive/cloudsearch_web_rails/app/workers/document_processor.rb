class DocumentProcessor < BaseIndexer
  include Sidekiq::Worker

  attr_accessor :document

  def perform(opts)
    super(opts)
    @document = Document.find(opts[:id])
  end

  def iterator
    [@document]
  end

end