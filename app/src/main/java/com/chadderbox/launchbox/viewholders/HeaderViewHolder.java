package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView mHeader;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        mHeader = (TextView) itemView;
    }

    public void bind(String suggestion) {
        mHeader.setText(suggestion);
    }
}
