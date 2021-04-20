class DropboxIndexer
  include Sidekiq::Worker
  sidekiq_options backtrace: true, retry: 1, unique: true, queue: :dropbox

  def perform(opts)
    opts.symbolize_keys!
    @account = Account.find(opts[:account_id])
    @account.update_attribute(:synced_at, Time::now)
    @client = @account.api_client

    @pool = Thread.pool(10)

    parse_folder(path: "/")
  end


  def parse_folder(opts)
    folder = @client.metadata(opts[:path])
    doc = touch_or_create(opts[:parent], opts, folder)
    # TODO if modified matches current on db, don't go in
    # TODO delete deleted
    contents = folder["contents"]
    contents.each do |content|
      if content["is_dir"]
        @pool.process do
          parse_folder(opts.merge(
                         path: content["path"],
                         parent_id: doc.id)
          )
        end
      else
        touch_or_create(doc, opts, content)
      end
    end
  end

  def touch_or_create(parent, opts, attrs)
    doc = DropboxDocument.update_or_create({
                                             account: @account,
                                             path: attrs['path'],
                                             name: File.basename(attrs['path']),
                                             parent: parent,
                                             metadata: {
                                               size: attrs['bytes'],  # TODO don't index size-zero
                                               modified: attrs['modified'],
                                               revision: attrs['revision'],
                                               hash: attrs['hash'],
                                               rev: attrs['rev'],
                                               file: !attrs['is_dir']
                                             }
                                           })
    DropboxDocumentDownloader.perform_async(opts.merge(id: doc.id)) if doc.downloadable? && !doc.downloaded?
    doc
  end
end
