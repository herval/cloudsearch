module EncodingHelper
  include ActionView::Helpers::SanitizeHelper

  def decode_ascii(txt)
    # http://yehudakatz.com/2010/05/05/ruby-1-9-encodings-a-primer-and-the-solution-for-rails/
    if txt.try(:encoding) != "UTF-8"
      txt.force_encoding("ISO-8859-1").encode("UTF-8")
    else
      txt
    end
  end

  def strip_everything(txt)
    return "" if txt.blank?
    begin
      txt = decode_ascii(txt)
      txt = HTMLEntities.new.decode(txt)
      txt = ActionController::Base.helpers.strip_tags(txt)
      txt = txt.gsub(/\s{1,}/, ' ')
      txt
    rescue => e
      puts e
      puts e.backtrace.join("\n")
      txt
    end
  end

end
