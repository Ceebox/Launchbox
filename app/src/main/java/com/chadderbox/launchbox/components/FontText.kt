package com.chadderbox.launchbox.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.chadderbox.launchbox.settings.SettingsManager
import com.chadderbox.launchbox.utils.ThemeHelper

@Composable
fun FontText(
    text: String,
    modifier: Modifier = Modifier,
    isHeading: Boolean = false,
    overrideFontSize: TextUnit? = null,
    color: Color? = null,
    style: TextStyle
) {
    val context = LocalContext.current

    // Theme-aware color if not overridden
    val textColor = color ?: Color(
        ThemeHelper.resolveColorAttr(context, android.R.attr.textColorPrimary)
    )

    val fontFamily = remember {
        val fontName = SettingsManager.getFont()
        when (fontName.lowercase()) {
            "sans-serif" -> FontFamily.SansSerif
            "serif" -> FontFamily.Serif
            "monospace" -> FontFamily.Monospace
            else -> {
                try {
                    FontFamily(Font("fonts/$fontName.ttf", context.assets))
                } catch (_: Exception) {
                    FontFamily.Default
                }
            }
        }
    }

    val settingFontSize = SettingsManager.getFontSize()
    val fontSize = overrideFontSize ?: if (isHeading) settingFontSize.sp * 1.5f else settingFontSize.sp
    val fontWeight = if (isHeading) FontWeight.Bold else FontWeight.Normal

    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight
        )
    )

    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle
    )
}
