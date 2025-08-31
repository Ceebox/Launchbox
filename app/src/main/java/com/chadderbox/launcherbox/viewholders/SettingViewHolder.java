package com.chadderbox.launcherbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.R;
import com.chadderbox.launcherbox.data.SettingItem;

import java.util.function.Consumer;

public class SettingViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

    public SettingViewHolder(@NonNull View itemView, Consumer<SettingItem> clickListener) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_name);

        itemView.setOnClickListener(v -> {
            var item = (SettingItem) v.getTag();
            if (item != null && clickListener != null) {
                clickListener.accept(item);
            }
        });
    }

    public void bind(SettingItem item) {
        itemView.setTag(item);

        var context = itemView.getContext();
        ImageView icon = itemView.findViewById(R.id.item_icon);
        icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_settings));
        textView.setText(item.getTitle());
    }
}
