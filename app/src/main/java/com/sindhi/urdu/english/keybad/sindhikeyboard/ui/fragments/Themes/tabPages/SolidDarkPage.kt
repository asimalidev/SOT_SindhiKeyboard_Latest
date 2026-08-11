package com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.Themes.tabPages


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.mySolidDarkThemes
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme
import com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardComponents.ThemeItem


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SolidDarkPage(
    currentSelectedTheme: String?,
    onApplyThemeClick: (CustomTheme) -> Unit,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding
    ) {
        items(mySolidDarkThemes) { themes ->

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                themes.forEach { theme ->

                    val isSelected = (currentSelectedTheme == theme.name)

                    ThemeItem(
                        backgroundColor = theme.backgroundColor,
                        backgroundColor2 = theme.backgroundColor2,
                        modifier = Modifier
                            .height(150.dp)
                            .weight(1f),
                        onClick = {
                            onApplyThemeClick(theme)
                        },
                        theme = theme,
                        isSelected = isSelected
                    )
                }
            }
        }
    }
}