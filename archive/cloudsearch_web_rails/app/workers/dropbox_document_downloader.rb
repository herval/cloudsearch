class DropboxDocumentDownloader < DocumentProcessor
  include EncodingHelper
  sidekiq_options backtrace: true, retry: 1, unique: true, queue: :dropbox

  def account_type
    DropboxAccount
  end

  def init_api_client
    account.api_client
  end

  def process(doc)
    if doc && doc.downloadable?
      # TODO set content type?

      puts "Downloading #{doc.id}" # TODO gsub?
      contents, metadata = @client.get_file_and_metadata(doc.path)
      doc.update_attributes(content: strip_everything(contents))
    end
  end
end
