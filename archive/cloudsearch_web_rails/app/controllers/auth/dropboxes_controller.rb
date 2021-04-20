class Auth::DropboxesController < Auth::AuthsController

  def new
    account = super
    redirect_to root_path
  end

  def account_class
    DropboxAccount
  end

  def build_attrs
    super
  end
end
