module SearchesHelper
  def with_highlights(result, field)
    result.highlights(field).collect do |highlight|
      highlight.format { |word| "<b>#{word}</b>" }
    end.join.html_safe
  end

  def with_or_without_highlights(result, field, max_size = nil)
    res = with_highlights(result, field)
    if res.blank?
      res = result.result.send(field)
    end
    max_size && truncate(res, length: max_size) || res
  end
end
