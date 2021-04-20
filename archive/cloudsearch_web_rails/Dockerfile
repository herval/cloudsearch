FROM phusion/passenger-ruby21

# Set correct environment variables.
ENV HOME /root

# Regenerate SSH host keys. baseimage-docker does not contain any, so you
# have to do that yourself. You may also comment out this instruction; the
# init system will auto-generate one during boot.
RUN /etc/my_init.d/00_regen_ssh_host_keys.sh

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]

# Start Nginx / Passenger
RUN rm -f /etc/service/nginx/down

# Remove the default site
RUN rm /etc/nginx/sites-enabled/default

# Add the nginx info
ADD nginx.conf /etc/nginx/sites-enabled/webapp.conf

ADD id_rsa.pub /tmp/your_key.pub
RUN cat /tmp/your_key.pub >> /root/.ssh/authorized_keys && rm -f /tmp/your_key.pub

RUN apt-get update && apt-get install wget curl build-essential git libxslt-dev libxml2 libxml2-dev libpq-dev libcurl4-openssl-dev -y

# pg_dump
RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" > /etc/apt/sources.list.d/pgdg.list
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
RUN apt-get update && apt-get install postgresql-client-9.4 -y

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Run Bundle in a cache efficient way
WORKDIR /tmp
COPY Gemfile /tmp/
COPY Gemfile.lock /tmp/
RUN bundle install

# Prepare folders
RUN mkdir /home/app/webapp

# Add the rails app
ADD . /home/app/webapp
WORKDIR /home/app/webapp

RUN chown -R app:app /home/app/webapp
RUN chmod -R 700 /home/app/webapp
RUN bundle exec rake tmp:cache:clear