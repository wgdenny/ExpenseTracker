package com.example.budgetnatorv2

import androidx.annotation.DrawableRes


sealed class BottomNavItem(val route: String, @DrawableRes val icon: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.baseline_home_24, "Home")
    object Transactions : BottomNavItem("transactions", R.drawable.baseline_payment_24, "Transactions")
    object Insights : BottomNavItem("insights", R.drawable.baseline_graphic_eq_24, "Insights")
    object Bills : BottomNavItem("bills", R.drawable.baseline_view_timeline_24, "Bills")

    object MainDestinations {
        const val UPCOMING_BILLS_ROUTE = "upcoming_bills"
        const val RECENT_TRANSACTIONS_ROUTE = "Recent_Transactions"
    }


    companion object {
        val items = listOf(Home, Transactions, Bills, Insights)
    }
}
