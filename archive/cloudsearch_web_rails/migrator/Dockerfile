FROM ubuntu:14.10

# Set correct environment variables.
ENV HOME /root

# Prepare folders
RUN mkdir -p /home/app/webapp

RUN apt-get install curl build-essential git libxslt-dev libxml2 libxml2-dev libpq-dev libcurl4-openssl-dev postgresql-client-9.4 -y

# Run Bundle in a cache efficient way
WORKDIR /tmp
COPY Gemfile /tmp/
COPY Gemfile.lock /tmp/
RUN bundle install

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Add the rails app
ADD . /home/app/webapp
RUN chown -R app:app /home/app
WORKDIR /home/app/webapp
