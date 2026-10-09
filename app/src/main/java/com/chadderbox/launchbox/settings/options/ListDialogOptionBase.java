package com.chadderbox.launchbox.settings.options;

import android.app.AlertDialog;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;

import java.util.Arrays;
import java.util.List;

public abstract class ListDialogOptionBase<T> implements ISettingOption {

    public abstract List<T> getOptions();

    public abstract List<String> getOptionDisplayNames();

    public abstract String getOptionSelectedMessage(T selectedItem);

    public abstract String onOptionSelected(T selectedItem);

    @Override
    public void performClick(SettingsActivity activity) {
        final var displayNames = getOptionDisplayNames().toArray(new String[0]);
        var optionList = getOptions();
        final var options = createGenericArray(optionList.size(), optionList.toArray());

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Theme")
            .setItems(displayNames, (dialog, which) -> {
                final var chosen = (T)options[which];
                onOptionSelected(chosen);

                Toast.makeText(activity, getOptionSelectedMessage(chosen), Toast.LENGTH_SHORT).show();

                activity.recreate();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @SafeVarargs
    private static <T> T[] createGenericArray(int length, T... array)
    {
        return Arrays.copyOf(array, length);
    }
}
