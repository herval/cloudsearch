class AccountsController < ApplicationController
  before_filter :authenticate_user!

  def index
    @accounts = current_user.accounts.active
  end

  def destroy
    current_user.accounts.find(params[:id]).disable!
    redirect_to accounts_path
  end
end
