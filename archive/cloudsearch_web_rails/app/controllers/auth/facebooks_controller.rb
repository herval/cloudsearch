class Auth::FacebooksController < Auth::AuthsController
  #skip_before_filter :authenticate_user!

  def new
    account = super
    redirect_to root_path
  end

  def account_class
    FacebookAccount
  end

end
