class SitesController < ApplicationController

  def new
    @account = Account.new(root_path: "")
  end

  def create
    # TODO validate
    @account = Account.create(user_id: current_user.id,
                              service: 'site', 
                              root_path: params[:account][:root_path] 
                              )

    SiteCrawler.perform_async @account.id

    redirect_to root_path
  end
end
