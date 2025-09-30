package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.APPEARANCE)
public final class TintIconsOption implements ISettingOption {

    @Override
    public String getTitle() {
        return "Tint Icons";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return getCurrentTintIconsMode(activity);
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        final var optionNames = new String[] {
            getIconModeName(SettingsManager.TINT_ICONS_DISABLED, activity),
            getIconModeName(SettingsManager.TINT_ICONS_MATCH_FONT, activity)
        };

        final var themePrefs = new int[] {
            SettingsManager.TINT_ICONS_DISABLED,
            SettingsManager.TINT_ICONS_MATCH_FONT
        };

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Tint Mode")
            .setItems(optionNames, (dialog, which) -> {
                final var chosen = themePrefs[which];
                SettingsManager.setTintIconsMode(chosen);

                Toast.makeText(activity, "Tint applied: " + optionNames[which], Toast.LENGTH_SHORT).show();

                activity.recreate();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private static String getCurrentTintIconsMode(final SettingsActivity context) {
        return getIconModeName(SettingsManager.getTintIconsMode(), context);
    }

    private static String getIconModeName(@SettingsManager.TintIconsMode final int tintIconsMode, final SettingsActivity context) {
        return switch (tintIconsMode) {
            case SettingsManager.TINT_ICONS_DISABLED -> context.getString(R.string.disabled);
            case SettingsManager.TINT_ICONS_MATCH_FONT -> context.getString(R.string.match_font);
            case SettingsManager.TINT_ICONS_SYSTEM -> context.getString(R.string.system);
            case SettingsManager.TINT_ICONS_PASTEL -> context.getString(R.string.pastel);
            // This shouldn't happen, but since we're really using an int, eh
            default -> context.getString(R.string.unknown);
        };
    }
}
