package com.chadderbox.launchbox.viewholders;

import android.view.View;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.ui.components.FontTextView;
import com.chadderbox.launchbox.settings.SettingsManager;

public final class HeaderViewHolder
    extends ViewHolderItemBase {

    public HeaderViewHolder(@NonNull View viewItem) {
        super(viewItem);

        var icon = itemView.findViewById(R.id.item_icon);
        icon.setVisibility(View.GONE);

        var text = (FontTextView) mText;
        text.setIsHeading(true);
        text.setTag(R.id.isHeading, true);

        // When we don't have icons, it looks weird to have everything floating
        var matchIconPadding = 16;
        if (SettingsManager.getIconPack().equals("None")) {
            matchIconPadding = 0;
        }

        text.setPadding(matchIconPadding, 16, matchIconPadding, 16);
    }

}
