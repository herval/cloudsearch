package cloudsearch.filter

import cloudsearch.content.Result

interface Filter {

    fun keep(r: Result): Boolean

    fun halt(): Boolean = false // should the entire thing stop?

}