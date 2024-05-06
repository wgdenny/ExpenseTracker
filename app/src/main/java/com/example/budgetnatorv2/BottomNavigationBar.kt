package com.example.budgetnatorv2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun BottomNavigationItem(item: BottomNavItem, navController: NavHostController) {
    val currentRoute = navController.currentDestination?.route
    val isSelected = item.route == currentRoute

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = {
                navController.navigate(item.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            })
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.title,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = item.title, textAlign = TextAlign.Center
        )
    }
}


//UI to the Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = BottomNavItem.items // Assuming you have this defined somewhere
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            BottomNavigationItem(item = item, navController = navController)
        }
    }
}