package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.utils.ShadowHelper;

public final class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView mHeader;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        mHeader = (TextView) itemView;

        ShadowHelper.applySettings(mHeader);
    }

    public void bind(String suggestion) {
        mHeader.setText(suggestion);
    }
}
