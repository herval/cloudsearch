class DocumentTree
  def initialize(root_path)
    @root_path = File.expand_path(root_path)
  end

  def parse!
    parse_dir(@root_path)
  end

  def parse_file(name, path)
    Document.touch_or_create(path)
  end

  def parse_dir(current_path)
    entries = Dir.entries(current_path)
    entries.each do |e|
      next if e == "." || e == ".."
      name = File.join(current_path, e)
      if File.directory?(name)
        parse_dir(name)
      else
        parse_file(e, name)
      end
    end
   end
end
