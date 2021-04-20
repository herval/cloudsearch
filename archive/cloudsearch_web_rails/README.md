foreman start -f Procfile.dev
foreman start -f Procfile.queues

# Starting up

docker-compose up

# Migrating db

docker-compose run web bundle exec rake db:migrate --trace
