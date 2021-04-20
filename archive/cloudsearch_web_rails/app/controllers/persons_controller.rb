class PersonsController < ApplicationController
  before_filter :authenticate_user!

  def index
    # TODO paginate
    @persons = current_user.persons.scoped.sort(description: 1)
  end

  def show
    @person = current_user.persons.find(params[:id])
  end

end
