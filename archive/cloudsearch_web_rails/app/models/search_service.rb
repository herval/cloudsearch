class SearchService

  def search(query, accounts)
    HTTParty.post("#{SearchConfig::API_BASE}/search/multi",
                  body: {
                      query: query,
                      accountTokens: Hash[accounts.map { |a| [a.service, a.token] }]
                  }.to_json,
                  headers: {
                      'Content-Type' => 'application/json'
                  })
  end

end