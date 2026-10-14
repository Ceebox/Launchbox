package com.chadderbox.launchbox.settings.options;

import android.app.AlertDialog;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;

import java.util.List;

public abstract class ListDialogOptionBase<T> implements ISettingOption {

    public abstract List<DialogOption<T>> getOptions();

    public abstract String getOptionSelectedMessage(T selectedItem);

    public abstract void onOptionSelected(T selectedItem);

    @Override
    public void performClick(SettingsActivity activity) {
        final var options = getOptions();
        final var items = options.stream().map(DialogOption::getItem).toArray();
        final var displayNames = options.stream()
            .map(DialogOption::getDisplayName)
            .toArray(String[]::new);

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Theme")
            .setItems(displayNames, (dialog, which) -> {
                @SuppressWarnings("unchecked") final var chosen = (T)items[which];
                onOptionSelected(chosen);

                Toast.makeText(activity, getOptionSelectedMessage(chosen), Toast.LENGTH_SHORT).show();

                activity.recreate();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
