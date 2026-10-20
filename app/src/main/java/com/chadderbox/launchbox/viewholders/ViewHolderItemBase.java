package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.main.adapters.CombinedAdapter;
import com.chadderbox.launchbox.ui.ShadowHelper;

public abstract class ViewHolderItemBase<T extends ListItem>
    extends RecyclerView.ViewHolder {

    protected final CombinedAdapter mAdapter;
    protected final TextView mText;

    public ViewHolderItemBase(@NonNull CombinedAdapter adapter, @NonNull View itemView) {
        super(itemView);

        mAdapter = adapter;
        mText = itemView.findViewById(R.id.item_name);
        ShadowHelper.applySettings(mText.getContext(), mText.getPaint());
    }

    public abstract void bind(T item);
}
