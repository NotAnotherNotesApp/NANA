package com.allubie.nana.ui.screens.expenses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconData(val name: String, val icon: ImageVector)

val CATEGORY_AVAILABLE_ICONS = listOf(
    CategoryIconData("ShoppingCart", Icons.Default.ShoppingCart),
    CategoryIconData("Fastfood", Icons.Default.Fastfood),
    CategoryIconData("LocalGasStation", Icons.Default.LocalGasStation),
    CategoryIconData("School", Icons.Default.School),
    CategoryIconData("Home", Icons.Default.Home),
    CategoryIconData("Work", Icons.Default.Work),
    CategoryIconData("Flight", Icons.Default.Flight),
    CategoryIconData("LocalHospital", Icons.Default.LocalHospital),
    CategoryIconData("SportsEsports", Icons.Default.SportsEsports),
    CategoryIconData("FitnessCenter", Icons.Default.FitnessCenter),
    CategoryIconData("Movie", Icons.Default.Movie),
    CategoryIconData("MusicNote", Icons.Default.MusicNote),
    CategoryIconData("Pets", Icons.Default.Pets),
    CategoryIconData("Build", Icons.Default.Build),
    CategoryIconData("AttachMoney", Icons.Default.AttachMoney)
)

val CATEGORY_DEFAULT_COLORS = listOf(
    "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FECA57",
    "#FF9FF3", "#54A0FF", "#5F27CD", "#00D2D3", "#FF9F43",
    "#10AC84", "#EE5A24", "#0984E3", "#6C5CE7", "#A29BFE"
)

fun getIconFromName(iconName: String): ImageVector = when (iconName) {
    "ShoppingCart" -> Icons.Default.ShoppingCart
    "Fastfood" -> Icons.Default.Fastfood
    "LocalGasStation" -> Icons.Default.LocalGasStation
    "School" -> Icons.Default.School
    "Home" -> Icons.Default.Home
    "Work" -> Icons.Default.Work
    "Flight" -> Icons.Default.Flight
    "LocalHospital" -> Icons.Default.LocalHospital
    "SportsEsports" -> Icons.Default.SportsEsports
    "FitnessCenter" -> Icons.Default.FitnessCenter
    "Movie" -> Icons.Default.Movie
    "MusicNote" -> Icons.Default.MusicNote
    "Pets" -> Icons.Default.Pets
    "Build" -> Icons.Default.Build
    "AttachMoney" -> Icons.Default.AttachMoney
    else -> Icons.Default.ShoppingCart
}
