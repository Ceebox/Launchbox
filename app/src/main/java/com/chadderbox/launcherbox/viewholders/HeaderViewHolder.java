package com.chadderbox.launcherbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView header;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        header = (TextView) itemView;
    }

    public void bind(String suggestion) {
        header.setText(suggestion);
    }
}
