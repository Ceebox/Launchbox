package com.chadderbox.launchbox.settings.options.implementation;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;
import com.chadderbox.launchbox.utils.FileHelpers;

public final class WallpaperOverrideOption
    implements ISettingOption {

    private final ActivityResultLauncher<Intent> mWallpaperPickerLauncher;

    public WallpaperOverrideOption(SettingsActivity activity) {
        mWallpaperPickerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    var selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {

                        // By selecting it, we gain permission to access it
                        // That's pretty neat actually, but it's gonna be super annoying if we don't save that permission
                        activity.getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        SettingsManager.setWallpaper(selectedImageUri.toString());
                        Toast.makeText(activity, "Wallpaper selected", Toast.LENGTH_SHORT).show();
                        activity.refreshOptions();
                    }
                }
            }
        );
    }

    @Override
    public String getTitle() {
        return "Wallpaper Override";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return getCurrentWallpaper(activity);
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showWallpaperDialog(activity);
    }

    private String getCurrentWallpaper(final SettingsActivity activity) {
        var wallpaper = FileHelpers.tryGetFileNameFromString(activity, SettingsManager.getWallpaper());
        if (wallpaper == null || wallpaper.isEmpty()) {
            return "None";
        }

        return wallpaper;
    }

    private void showWallpaperDialog(final SettingsActivity activity) {

        var options = new String[] {
            "Choose Wallpaper",
            "Clear Wallpaper"
        };

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Wallpaper")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mWallpaperPickerLauncher.launch(intent);
                        break;

                    case 1:
                        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
                            .setTitle("Clear Wallpaper")
                            .setMessage("Are you sure you want to remove the current wallpaper?")
                            .setPositiveButton("Yes", (d, w) -> {
                                SettingsManager.setWallpaper("");
                                Toast.makeText(activity, "Wallpaper cleared", Toast.LENGTH_SHORT).show();

                                activity.refreshOptions();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                        break;
                }

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
