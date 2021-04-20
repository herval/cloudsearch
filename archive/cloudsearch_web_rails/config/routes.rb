Cloudsearch::Application.routes.draw do

  devise_for :users

  resource :search do
    collection do
      get :similar
      get :person
    end
  end
  resources :accounts
  resources :sites
  resources :persons
  resources :documents

  root to: "searches#show"

  get "/auth/google_oauth2/callback", to: 'auth/googles#new'
  get "/auth/dropbox_oauth2/callback", to: 'auth/dropboxes#new'
  get "/auth/37signals/callback", to: 'auth/thirty_seven_signals#new'
  get "/auth/facebook/callback", to: 'auth/facebooks#new'
  get "/auth/linkedin/callback", to: 'auth/linkedins#new'
end
