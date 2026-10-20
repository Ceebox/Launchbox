package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.ui.ShadowHelper;

public abstract class ViewHolderItemBase
    extends RecyclerView.ViewHolder {
    protected final TextView mText;

    public ViewHolderItemBase(@NonNull View itemView) {
        super(itemView);
        mText = itemView.findViewById(R.id.item_name);

        ShadowHelper.applySettings(mText.getContext(), mText.getPaint());
    }

    public void bind(String text) {
        mText.setText(text);
        ShadowHelper.applySettings(mText.getContext(), mText.getPaint());
    }
}
