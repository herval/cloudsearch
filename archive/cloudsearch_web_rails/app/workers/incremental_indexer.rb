require 'thread/pool'

class IncrementalIndexer < BaseIndexer

  def iterate!
    iterator.each do |document|
      break if !above_cutoff?
      process(document)
    end
  end

  def above_cutoff?
    true
  end
end