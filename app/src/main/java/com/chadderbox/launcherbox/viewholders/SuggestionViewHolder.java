package com.chadderbox.launcherbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.R;

import java.util.function.Consumer;

public class SuggestionViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private String mSuggestion;

    public SuggestionViewHolder(@NonNull View itemView, Consumer<String> clickListener) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_name);

        itemView.setOnClickListener(v -> {
            if (mSuggestion != null && clickListener != null) {
                clickListener.accept(mSuggestion);
            }
        });
    }

    public void bind(String suggestion) {
        mSuggestion = suggestion;

        var context = itemView.getContext();
        ImageView icon = itemView.findViewById(R.id.item_icon);
        icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_browse_suggestion));
        textView.setText(suggestion);
    }
}
