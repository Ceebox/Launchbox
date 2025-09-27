package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.WebItem;
import com.chadderbox.launchbox.utils.TintHelper;

public final class WebViewHolder extends RecyclerView.ViewHolder {
    private final TextView mTextView;

    public WebViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.item_name);

        itemView.setOnClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performOpenAction(v.getContext());
            }
        });

        itemView.setOnLongClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performHoldAction(v.getContext());
                return true;
            }

            return false;
        });
    }

    public void bind(WebItem item) {
        final var query = item.getQuery();

        itemView.setTag(item);
        final var context = itemView.getContext();
        final var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        final var drawable = AppCompatResources.getDrawable(context, R.drawable.ic_browse);
        icon.setImageDrawable(TintHelper.tryTintIcon(context, drawable));
        mTextView.setText(context.getString(R.string.search_the_web_for, query));
    }
}
