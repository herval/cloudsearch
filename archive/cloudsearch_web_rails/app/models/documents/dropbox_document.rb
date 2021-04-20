class DropboxDocument < SearchableDocument
  store_accessor :metadata,
                    :size, :modified,
                    :rev, :file, :hash, :revision,
                    :content_type

  attr_accessor :content # TODO don't save this
  before_create :set_content_type

  searchable do
    text :title, more_like_this: true, boost: 4.0, stored: true
    text :summary, more_like_this: true, stored: true
    text :profiles, more_like_this: true do
      ""
    end
    string :person_ids, multiple: true do
    end
    string :user_id
    boost { file_relevancy }
  end
  # TODO boost down folders, up files

  belongs_to :parent, class_name: 'DropboxDocument', inverse_of: :children
  has_many :children, class_name: 'DropboxDocument', inverse_of: :parent, dependent: :destroy

  def self.update_or_create(opts)
    account = opts.delete(:account)
    opts.reject! { |_, v| v.blank? }

    doc = DropboxDocument.where(account: account, uid: opts[:path]).first
    if doc
      opts.reject! { |k, v| doc[k] == v } # reject attributes that didn't change
      if !opts.blank?
        opts[:content] = nil if opts[:hash] != doc.hash # this will redownload the body
        puts "Updating: #{doc.path} #{opts}"
        opts.each do |k, v| # there's a bug on update_attribute with json attributes in rails
          doc.send("#{k}=", v)
        end
        doc.save if doc.changed?
      #else
      #  puts "No changes: #{doc.path}"
      end
    else
      puts "Creating: #{opts[:path]}"
      doc = DropboxDocument.create(opts.merge(uid: opts[:path], account: account, parent: parent))
    end

    doc
  end

  def url
    client = self.account.api_client
    client.shares(self.path)['url']
  end

  def title
    self.name
  end

  def summary
    self.content
  end

  def downloadable?
    return false if !self.file
    content_type && content_type.split("/")[0] == "text"
  end

  def downloaded?
    self.content.nil?
  end

  def file_relevancy
    if downloadable?
      4
    elsif file
      2
    else
      0.5
    end
  end

  def set_content_type
    type = MIME::Types.type_for(self.path).first
    self.content_type = type.try(:content_type)
  end
  #def self.touch_or_create(attrs)
  #  raise "Implement this on subclasses!"
  #  #doc = Document.where(path: path).first
  #  #attrs = {
  #  #  name: File.basename(path),
  #  #  #size: File.size(path).to_f / 2**20,
  #  #  #modified: File.mtime(path),
  #  #  #created: File.ctime(path),
  #  #  #md5: Digest::MD5.file(path).hexdigest
  #  #}
  #  #
  #  #if doc
  #  #  doc.update_attributes(attrs)
  #  #else
  #  #  doc = Document.create(attrs.merge(path: path))
  #  #end
  #  #
  #  #doc
  #end
  #
  #def content
  #  # TODO only index the text (remove \n, etc?)
  #  # don't index binary and big files
  #  (File.binary?(self.path) || self.size.to_i > 5) ? "" : File.read(self.path)
  #end
end
