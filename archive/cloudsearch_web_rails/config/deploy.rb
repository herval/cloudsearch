# require "whenever/capistrano"
# require "bundler/capistrano"

# set :whenever_environment, :production

set :keep_releases, 2

set :application, "cloudsearch"
set :scm, "git"
set :repository, "git@bitbucket.org:herval/cloudsearch.git"

set :user, 'ubuntu'
set :use_sudo, false
set :deploy_to, '/home/ubuntu/cloudsearch'

default_run_options[:shell] = '/bin/bash'
set :shell, "/usr/bin/bash"
# set :bundle_cmd, 'source $HOME/.bash_profile && bundle'

set :scm, :none
set :deploy_via, :copy
set :ssh_options, { forward_agent: true, auth_methods: ['publickey'], keys: [File.join(ENV["HOME"], ".ssh", "amazon-herval-razorblade.pem")] }
default_run_options[:pty] = true

role :web, "107.22.163.85"                          # Your HTTP server, Apache/etc
role :app, "107.22.163.85"                          # This may be the same as your `Web` server
role :db, "107.22.163.85"

# if you want to clean up old releases on each deploy uncomment this:
# after "deploy:restart", "deploy:cleanup"


# task :restart_processes, :role => :app do
  # run("cd #{deploy_to} && cp sh.pemared/config/database.yml current/config/")
  # run("cd #{deploy_to} && cp shared/config/mailer.yml current/config/")

  # run("killall -s SIGTERM `pgrep -f scheduling_daemon`") rescue p "scheduling_daemon not running"
  # run("(cd #{deploy_to}/current && /usr/bin/env RACK_ENV=production nohup ./bin/scheduling_daemon > log/daemon.log 2>&1 &) && sleep 1", :pty => true)
# end
# after 'deploy:restart', :restart_processes

