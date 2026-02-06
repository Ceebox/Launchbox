package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SuggestionItem;
import com.chadderbox.launchbox.ui.TintHelper;

public final class SuggestionViewHolder extends ViewHolderItemBase {

    public SuggestionViewHolder(@NonNull View itemView) {
        super(itemView);

        itemView.setOnClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performOpenAction(v);
            }
        });

        itemView.setOnLongClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performHoldAction(v);
                return true;
            }

            return false;
        });
    }

    public void bind(SuggestionItem item) {
        var suggestion = item.getSuggestion();
        itemView.setTag(item);

        var context = itemView.getContext();
        var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        var drawable = AppCompatResources.getDrawable(context, R.drawable.ic_browse_suggestion);
        icon.setImageDrawable(TintHelper.tryTintIcon(context, drawable));
        mText.setText(suggestion);
    }
}
