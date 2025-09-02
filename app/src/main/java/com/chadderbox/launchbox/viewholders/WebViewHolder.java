package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;

import java.util.function.Consumer;

public class WebViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private String mQuery;

    public WebViewHolder(@NonNull View itemView, Consumer<String> clickListener) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_name);

        itemView.setOnClickListener(v -> {
            if (mQuery != null && clickListener != null) {
                clickListener.accept(mQuery);
            }
        });
    }

    public void bind(String query) {
        mQuery = query;

        var context = itemView.getContext();
        var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_browse));
        textView.setText(context.getString(R.string.search_the_web_for, query));
    }
}
