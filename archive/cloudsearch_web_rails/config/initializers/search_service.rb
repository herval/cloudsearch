module SearchConfig
  API_BASE = if Rails.env.production?
               "wss://secure.novelo.com"
             else
               "ws://localhost:8080"
             end
end