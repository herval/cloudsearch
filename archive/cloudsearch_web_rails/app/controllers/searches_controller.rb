class SearchesController < ApplicationController
  before_filter :authenticate_user!

  def show
    if !params[:q].blank?
      @results = SearchService.new.search(params[:q], current_user.accounts)
      respond_to do |format|
        format.html
        format.json { render json: @results }
      end
    end
  end

end
