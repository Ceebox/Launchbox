package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SuggestionItem;

import java.util.function.Consumer;

public class SuggestionViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private String mSuggestion;

    public SuggestionViewHolder(@NonNull View itemView) {
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

    public void bind(SuggestionItem item) {
        mSuggestion = item.getSuggestion();
        itemView.setTag(item);

        var context = itemView.getContext();
        var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_browse_suggestion));
        textView.setText(mSuggestion);
    }
}
