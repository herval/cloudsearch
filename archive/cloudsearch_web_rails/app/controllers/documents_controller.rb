class DocumentsController < ApplicationController
  before_filter :authenticate_user!

  def show
    @document = params[:type].constantize.find(params[:id])
    # TODO scope by user

    url = @document.url
    redirect_to url
  end
end
