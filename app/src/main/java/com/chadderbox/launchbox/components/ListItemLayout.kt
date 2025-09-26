package com.chadderbox.launchbox.components

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.chadderbox.launchbox.R
import com.chadderbox.launchbox.data.AppItem
import com.chadderbox.launchbox.data.SettingItem
import com.chadderbox.launchbox.data.SuggestionItem
import com.chadderbox.launchbox.data.WebItem
import com.chadderbox.launchbox.settings.SettingsManager
import com.chadderbox.launchbox.utils.IconPackLoader
import com.chadderbox.launchbox.utils.compose.BitmapHelper

fun ComposeView.setAppRow(appItem: AppItem, iconPackLoader: IconPackLoader) {
    val state = initListItemComposition()

    val painter: Painter? = BitmapHelper.bitmapToBitmapPainter(
        iconPackLoader.loadAppIconBitmap(appItem.appInfo.packageName, appItem.appInfo.category)
    )

    state.text.value = appItem.appInfo.label
    state.painter.value = painter
    state.onClick.value = { appItem.performOpenAction(context) }
    state.onLongClick.value = { appItem.performHoldAction(context) }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun ComposeView.setWebRow(webItem: WebItem) {
    val state = initListItemComposition()
    val drawable = context.getDrawable(R.drawable.ic_browse_suggestion)
    val painter = drawable?.let { BitmapHelper.drawableToPainter(it) }

    state.text.value = webItem.query
    state.painter.value = painter
    state.onClick.value = { webItem.performOpenAction(context) }
    state.onLongClick.value = { webItem.performHoldAction(context) }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun ComposeView.setSuggestionRow(suggestionItem: SuggestionItem) {
    val state = initListItemComposition()
    val drawable = context.getDrawable(R.drawable.ic_browse_suggestion)
    val painter = drawable?.let { BitmapHelper.drawableToPainter(it) }

    state.text.value = suggestionItem.suggestion
    state.painter.value = painter
    state.onClick.value = { suggestionItem.performOpenAction(context) }
    state.onLongClick.value = { suggestionItem.performHoldAction(context) }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun ComposeView.setSettingRow(settingItem: SettingItem) {
    val state = initListItemComposition()
    val drawable = context.getDrawable(R.drawable.ic_settings)
    val painter = drawable?.let { BitmapHelper.drawableToPainter(it) }

    state.text.value = settingItem.title
    state.painter.value = painter
    state.onClick.value = { settingItem.performOpenAction(context) }
    state.onLongClick.value = { settingItem.performHoldAction(context) }
}

/**
 * Generic composable for a row with an icon and text, supporting optional long click
 */
@Composable
fun ListItemContent(
    text: String,
    painter: Painter?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val matchIconPadding = if (SettingsManager.getIconPack() == "None") 0.dp else 16.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(min = 60.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter != null) {
            val iconSize = 40.dp
            val outlineThickness = 2.dp

            Box(
                modifier = Modifier
                    .size(iconSize),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painter,
                    contentDescription = text,
                    modifier = Modifier.size(iconSize - outlineThickness)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(matchIconPadding))
        }

        FontText(
            text = text,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0x55000000),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

// Random
private const val TAG_LIST_ITEM_STATE = -7896345

class ListItemRowState(
    initialText: String = "",
    initialPainter: Painter? = null
) {
    var text: MutableState<String> = mutableStateOf(initialText)
    var painter: MutableState<Painter?> = mutableStateOf(initialPainter)
    var onClick: MutableState<(() -> Unit)?> = mutableStateOf(null)
    var onLongClick: MutableState<(() -> Unit)?> = mutableStateOf(null)
}

/**
 * Update the state for an existing composition (safe to call from onBindViewHolder).
 * This does not recreate the composition — it just updates the MutableState values that the composition observes.
 */
fun updateListItemRow(
    composeView: ComposeView,
    text: String,
    painter: Painter?,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?
) {
    val tag = composeView.getTag(TAG_LIST_ITEM_STATE)
    val state = tag as? ListItemRowState ?: composeView.initListItemComposition()

    state.text.value = text
    state.painter.value = painter
    state.onClick.value = onClick
    state.onLongClick.value = onLongClick
}

/**
 * Call once (e.g. in onCreateViewHolder) to initialise the composition for this ComposeView.
 * Returns the created state object (also stored as a tag on the view).
 */
fun ComposeView.initListItemComposition(): ListItemRowState {
    // Prevent Android View state (SavedStateRegistry) from being used for recycled views
    isSaveEnabled = false

    val lifecycleOwner: LifecycleOwner? = (this as View).findViewTreeLifecycleOwner()
    if (lifecycleOwner != null) {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner))
    } else {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    val existing = getTag(TAG_LIST_ITEM_STATE)
    if (existing is ListItemRowState) return existing

    val state = ListItemRowState()
    setTag(TAG_LIST_ITEM_STATE, state)

    setContent {
        ListItemContent(
            text = state.text.value,
            painter = state.painter.value,
            onClick = state.onClick.value ?: {},
            onLongClick = state.onLongClick.value
        )
    }

    return state
}