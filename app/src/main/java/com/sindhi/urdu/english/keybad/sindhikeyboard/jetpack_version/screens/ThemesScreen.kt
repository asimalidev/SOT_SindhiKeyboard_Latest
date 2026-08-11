package com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sindhi.urdu.english.keybad.R
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.preferences.MyPreferences
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes.tabPages.DefaultPage
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes.tabPages.GradientPage
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes.tabPages.SolidDarkPage
import com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes.tabPages.SolidLightPage
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemesScreen(
    context: Context,
    onApplyThemeClick: (CustomTheme) -> Unit
) {
    val myPreferences = MyPreferences(context)
    val titleAndIcons = listOf("Default", "Gradient", "Dark", "Light")
    val lexendFontFamily = FontFamily(
        Font(R.font.lexend_medium, FontWeight.Medium)
    )
    // Pager state to control the swiping behavior
    val pagerState = rememberPagerState(pageCount = { titleAndIcons.size })
    val coroutineScope = rememberCoroutineScope()

    // 1. The single source of truth for all tabs (State Hoisting)
    var currentSelectedTheme by remember { mutableStateOf(myPreferences.getTheme()) }

    Scaffold(
        containerColor = colorResource(id = R.color.home_bg_color),
        topBar = {
            Column {
                // ScrollableTabRow prevents text from cutting off by sizing tabs to their content
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    edgePadding = 8.dp,
                    indicator = { }, // Removes the default bottom line indicator
                    divider = { }    // Removes the default divider line
                ) {
                    titleAndIcons.forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index

                        Tab(
                            selected = isSelected,
                            onClick = {
                                // Animate the pager when a tab is directly clicked
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            // Removes the gray ripple effect when clicking tabs (optional)
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // Custom pill design for the tabs
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        color = if (isSelected) Color(0xFFC80028) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) Color.Black else Color.Transparent,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontFamily = lexendFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Place your AdMob banner here.
        }
    ) { innerPadding ->

        // HorizontalPager enables the left-to-right swiping gestures
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->

            // 2. Intercept the click to update our hoisted state before passing it to the Fragment
            val handleThemeClick: (CustomTheme) -> Unit = { theme ->
                currentSelectedTheme = theme.name // Updates the UI instantly across all tabs
                onApplyThemeClick(theme)          // Triggers your actual save/navigation logic
            }

            // Render the specific page content based on the current horizontal pager index
            when (page) {
                0 -> DefaultPage(currentSelectedTheme, handleThemeClick, PaddingValues(0.dp))
                1 -> GradientPage(currentSelectedTheme, handleThemeClick, PaddingValues(0.dp))
                2 -> SolidDarkPage(currentSelectedTheme, handleThemeClick, PaddingValues(0.dp))
                3 -> SolidLightPage(currentSelectedTheme, handleThemeClick, PaddingValues(0.dp))
            }
        }
    }
}