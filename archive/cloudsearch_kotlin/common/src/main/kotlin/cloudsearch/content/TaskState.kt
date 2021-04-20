package cloudsearch.content

import cloudsearch.account.AccountType
import org.joda.time.DateTime

enum class TaskState(v: String) {
    Open("Open"),
    Closed("Closed"),
    Cancelled("Cancelled"),
    InProgress("InProgress"),
    Unknown("Unknown")
}

