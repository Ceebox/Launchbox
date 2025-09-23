package com.chadderbox.launchbox.settings.options.implementation;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

public final class FontSizeOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Font Size";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return String.valueOf(SettingsManager.getFontSize());
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showFontSizeDialog(activity);
    }

    private void showFontSizeDialog(final SettingsActivity activity) {
        var seekBar = new SeekBar(activity);
        seekBar.setMax(32 - 14);
        seekBar.setProgress(SettingsManager.getFontSize() - 14);
        seekBar.setPadding(40, 40, 40, 40);

        var preview = new TextView(activity);
        preview.setText(activity.getString(R.string.font_size_pick, String.valueOf(SettingsManager.getFontSize())));
        preview.setTextSize(16);
        preview.setPadding(40, 20, 40, 20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                var fontSize = 14 + progress;
                preview.setText(activity.getString(R.string.font_size_pick, String.valueOf(fontSize)));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        var layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(preview);
        layout.addView(seekBar);

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Font Size")
            .setView(layout)
            .setPositiveButton("Ok", (dialog, which) -> {
                var selectedSize = 14 + seekBar.getProgress();
                if (selectedSize == SettingsManager.getFontSize()) {
                    return;
                }

                SettingsManager.setFontSize(selectedSize);
                Toast.makeText(activity, "Size applied: " + selectedSize, Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
