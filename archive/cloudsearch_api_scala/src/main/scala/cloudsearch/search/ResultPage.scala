package cloudsearch.search

import cloudsearch.db.Account

/**
 * Created by herval on 7/30/15.
 */
case class ResultPage(account: Account, resultType: String, results: List[Result]) {
}
