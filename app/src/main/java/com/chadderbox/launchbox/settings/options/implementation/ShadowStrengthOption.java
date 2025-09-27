package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.components.FontTextView;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.APPEARANCE)
public final class ShadowStrengthOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Shadow Strength";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return String.valueOf(SettingsManager.getShadowStrength());
    }

    @Override
    public void performClick(SettingsActivity activity) {
        showShadowStrengthDialog(activity);
    }

    private void showShadowStrengthDialog(SettingsActivity activity) {
        var seekBar = new SeekBar(activity);
        seekBar.setMin(0);
        seekBar.setMax(6);
        seekBar.setProgress(SettingsManager.getShadowStrength());
        seekBar.setPadding(40, 40, 40, 40);

        var preview = new FontTextView(activity);
        preview.setText(activity.getString(R.string.shadow_strength_pick, String.valueOf(SettingsManager.getShadowStrength())));
        preview.setTextSize(16);
        preview.setPadding(40, 20, 40, 20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preview.setText(activity.getString(R.string.shadow_strength_pick, String.valueOf(progress)));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        var layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(preview);
        layout.addView(seekBar);

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Shadow Strength")
            .setView(layout)
            .setPositiveButton("Ok", (dialog, which) -> {
                var newStrength = seekBar.getProgress();
                if (newStrength == SettingsManager.getShadowStrength()) {
                    return;
                }

                // Remember to convert back from percentage
                SettingsManager.setShadowStrength(newStrength);
                Toast.makeText(activity, activity.getString(R.string.shadow_strength_pick, String.valueOf(newStrength)), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

}
