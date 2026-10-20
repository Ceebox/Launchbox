package com.chadderbox.launchbox.settings.options.implementation;

import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.data.TextAlignmentType;
import com.chadderbox.launchbox.settings.options.DialogOption;
import com.chadderbox.launchbox.settings.options.ListDialogOptionBase;

import java.util.Arrays;
import java.util.List;

public final class TextAlignmentOption extends ListDialogOptionBase<TextAlignmentType> {

    @Override
    public String getTitle() {
        return "Text Alignment";
    }

    @Override
    public String getDialogTitle() {
        return "Select Alignment";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return SettingsManager.getTextAlignment().getValue();
    }

    @Override
    public List<DialogOption<TextAlignmentType>> getOptions() {
        return Arrays.stream(TextAlignmentType.values())
            .map(type -> new DialogOption<>(type, type.getValue()))
            .toList();
    }

    @Override
    public String getOptionSelectedMessage(TextAlignmentType selectedItem) {
        return "Text Alignment: " + selectedItem.getValue();
    }

    @Override
    public void onOptionSelected(TextAlignmentType selectedItem) {
        SettingsManager.setTextAlignment(selectedItem);
    }
}
