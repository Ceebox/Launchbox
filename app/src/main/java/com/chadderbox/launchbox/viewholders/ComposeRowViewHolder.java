package com.chadderbox.launchbox.viewholders;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.compose.ui.platform.ComposeView;
import androidx.compose.ui.platform.ViewCompositionStrategy;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;

public class ComposeRowViewHolder extends RecyclerView.ViewHolder {
    private final ComposeView mComposeView;

    public ComposeRowViewHolder(@NonNull View itemView) {
        super(itemView);
        mComposeView = itemView.findViewById(R.id.compose_item);
        mComposeView.setBackgroundColor(Color.TRANSPARENT);

        // Try to prevent strange save state registry errors
        mComposeView.setId(View.generateViewId());
        mComposeView.setSaveEnabled(false);

        var lifeCycleOwner = ViewTreeLifecycleOwner.get(mComposeView);
        if (lifeCycleOwner != null) {
            mComposeView.setViewCompositionStrategy(new ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifeCycleOwner));
        }
    }

    public ComposeView getComposeView() {
        return mComposeView;
    }
}
