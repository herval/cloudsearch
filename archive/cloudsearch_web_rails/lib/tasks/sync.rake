namespace :sync do

  task :pending => :environment do
    Account.all.each do |a|
      if a.pending_sync?
        puts "Scheduling: #{a.id.to_s} #{a.description}"
        a.spawn_sync_jobs!
      else
        puts "Skipping: #{a.id.to_s} #{a.description}"
      end
    end
  end
end