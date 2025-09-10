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

import java.util.function.Consumer;

public class WebViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private String mQuery;

    public WebViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_name);

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
        mQuery = item.getQuery();

        itemView.setTag(item);
        var context = itemView.getContext();
        var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        var drawable = AppCompatResources.getDrawable(context, R.drawable.ic_browse);
        icon.setImageDrawable(TintHelper.tryTintIcon(context, drawable));
        textView.setText(context.getString(R.string.search_the_web_for, mQuery));
    }
}
