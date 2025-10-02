package com.chadderbox.launchbox.main.commands;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.ui.components.FontEditText;
import com.chadderbox.launchbox.utils.AppAliasProvider;

public final class RenameCommand
    implements IDialogCommand {

    private final AppInfo mAppInfo;

    public RenameCommand(AppInfo appInfo) {
        mAppInfo = appInfo;
    }

    @Override
    public String getName() {
        return "Rename" + mAppInfo.getLabel();
    }

    @Override
    public void execute() {
        showAliasDialog();
    }

    private void showAliasDialog() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        var textEditor = new FontEditText(activity);
        textEditor.setHint(mAppInfo.getName());
        textEditor.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        var padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
        textEditor.setMinWidth((int) (240 * activity.getResources().getDisplayMetrics().density));
        textEditor.setPadding(padding, padding, padding, padding);
        textEditor.setSingleLine(true);

        // Make this single line and prevent adding a new line
        textEditor.setMaxLines(1);
        textEditor.setLines(1);
        textEditor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        textEditor.setImeOptions(EditorInfo.IME_ACTION_DONE);

        var layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(textEditor);

        var aliasDialog = new android.app.AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Rename " + mAppInfo.getLabel())
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok", (otherDialogInternal, otherWhich) -> {
                var newAlias = textEditor.getText().toString();
                var aliasProvider = ServiceManager.getService(AppAliasProvider.class);
                aliasProvider.setAlias(mAppInfo.getPackageName(), newAlias);
                mAppInfo.setAlias(newAlias);

                activity.refreshUi();
            })
            .create();

        aliasDialog.show();

        // Try to get the keyboard up
        textEditor.requestFocus();
        textEditor.post(() -> {
            var imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(textEditor, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        final var positiveButton = aliasDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        // We don't want to allow this if the text is empty
        textEditor.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                var input = s.toString().trim();
                positiveButton.setEnabled(!input.isEmpty());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
