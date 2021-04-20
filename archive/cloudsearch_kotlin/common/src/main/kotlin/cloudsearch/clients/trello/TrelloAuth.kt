package cloudsearch.clients.trello

//class TrelloAuth(val search: SearchBuilder) : StandardOauth2Auth<TrelloAccount>(
//        accountType = TrelloAccount::class,
//        authorizePath = "https://trello.com/1/authorize",
//        accessTokenPath = "https://trello.com/1/OAuthGetAccessToken",
//        scopes = listOf("user:email", "read:user", "read:org"),
//        server = server,
//        credentials = creds,
//        jsonParser = Parsers.underscored,
//        builder = {
//            TrelloAccount(
//                    token = it.get("access_token") as String,
//                    username = "NOT SET",
//                    nodeId = "NOT SET"
//            )
//        }
//) {
//
//}