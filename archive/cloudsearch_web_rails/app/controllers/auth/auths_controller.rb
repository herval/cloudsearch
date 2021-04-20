class Auth::AuthsController < ApplicationController
  before_filter :authenticate_user!

  def new
    attrs = build_attrs

    account = current_user.accounts.where(service: attrs[:service],
                                          email: attrs[:email]).
                                    first

    if account
      account.update_attributes(attrs.merge(state: "active"))
    else
      account = account_class.create({ user: current_user }.merge(attrs))
    end

    account
  end


  def auth
    request.env['omniauth.auth']
  end

  def account_class
    raise "Implement in subclasses!"
  end

  def build_attrs
    attrs = {
      service: auth['provider'],
      auth_code: params[:code] || params[:oauth_token],
      uid: auth['uid'],
      token: auth['credentials']['token'],
      secret: auth['credentials']['secret'],
      email: auth['info']['email'],
      name: auth['info']['name'],
      metadata: auth['extra']['raw_info'].as_json
    }
    attrs[:token_expires_at] = Time.at(auth['credentials']['expires_at']) if auth['credentials']['expires_at']
    attrs[:refresh_token] = auth['credentials']['refresh_token'] unless auth['credentials']['refresh_token'].blank?
    attrs
  end
end
