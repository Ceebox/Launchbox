package com.chadderbox.launchbox.viewholders;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.ui.ShadowHelper;

public abstract class ViewHolderItemBase
    extends RecyclerView.ViewHolder
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected final TextView mText;

    public ViewHolderItemBase(@NonNull View itemView) {
        super(itemView);
        mText = itemView.findViewById(R.id.item_name);

        SettingsManager.registerChangeListener(this);
        ShadowHelper.applySettings(mText.getContext(), mText.getPaint());
    }

    public void bind(String text) {
        mText.setText(text);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        whenSharedPreferencesChanged(key);
    }

    protected void whenSharedPreferencesChanged(String key) {
        if (SettingsManager.KEY_SHADOW_STRENGTH.equals(key)) {
            ShadowHelper.applySettings(mText.getContext(), mText.getPaint());
        }
    }
}
