package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.data.TextAlignmentType;
import com.chadderbox.launchbox.settings.options.ListDialogOptionBase;

import java.util.Collections;
import java.util.List;

public class TextAlignmentOption extends ListDialogOptionBase<TextAlignmentType> {

    @Override
    public String getTitle() {
        return "Text Alignment";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return SettingsManager.getTextAlignment().getValue();
    }

    @Override
    public List<TextAlignmentType> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOptionDisplayNames() {
        return Collections.emptyList();
    }

    @Override
    public String getOptionSelectedMessage(TextAlignmentType selectedItem) {
        return "";
    }

    @Override
    public String onOptionSelected(TextAlignmentType selectedItem) {
        return "";
    }

    @Override
    public void performClick(SettingsActivity activity) {
        final var themes = new String[] { "System Default", "Light", "Dark" };
        final var themePrefs = new int[] { Configuration.UI_MODE_NIGHT_UNDEFINED, Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES };

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Theme")
            .setItems(themes, (dialog, which) -> {
                final var chosen = themePrefs[which];
                SettingsManager.setTheme(chosen);

                Toast.makeText(activity, "Theme applied: " + themes[which], Toast.LENGTH_SHORT).show();

                activity.recreate();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
