class Auth::ThirtySevenSignalsController < Auth::AuthsController

  def new
    account = super

    redirect_to root_path
  end

  def account_class
    ThirtySevenSignalsAccount
  end

end

