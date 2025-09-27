package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView mHeader;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        mHeader = (TextView) itemView;

        // These are copied from list_item_layout
        var shadowColor = ContextCompat.getColor(mHeader.getContext(), R.color.text_shadow);
        mHeader.setShadowLayer(2f, 4f, 4f, shadowColor);
    }

    public void bind(String suggestion) {
        mHeader.setText(suggestion);
    }
}
