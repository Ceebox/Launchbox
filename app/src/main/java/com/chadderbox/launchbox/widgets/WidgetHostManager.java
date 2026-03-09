package com.chadderbox.launchbox.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.widgets.data.WidgetDao;
import com.chadderbox.launchbox.widgets.data.WidgetDatabase;
import com.chadderbox.launchbox.widgets.data.WidgetItem;

import java.util.List;
import java.util.concurrent.Executors;

public final class WidgetHostManager {

    private static final int APPWIDGET_HOST_ID = 1024;
    private static final int CELL_DP = 70;
    private static final int CELL_PADDING_DP = 2;

    private final Activity mActivity;
    private final AppWidgetManager mAppWidgetManager;
    private final AppWidgetHost mAppWidgetHost;
    private final WidgetDao mWidgetDao;
    private ActivityResultLauncher<Intent> mConfigLauncher;
    private ActivityResultLauncher<Intent> mBindLauncher;
    private int mPendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public WidgetHostManager(Activity activity) {
        mActivity = activity;
        mAppWidgetManager = AppWidgetManager.getInstance(activity);
        mAppWidgetHost = new AppWidgetHost(activity, APPWIDGET_HOST_ID);
        mWidgetDao = WidgetDatabase.getDatabase(mActivity).widgetDao();
    }

    public void showCustomWidgetPicker() {
        var view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_widget_picker, null);
        var recyclerView = (RecyclerView) view.findViewById(R.id.picker_recycler);
        var dialog = new AlertDialog.Builder(mActivity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Widget")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create();

        var adapter = new WidgetGroupAdapter(WidgetHelpers.getGroupedWidgets(mActivity, mAppWidgetManager), info -> {
            dialog.dismiss();
            confirmWidgetSelection(info);
        });

        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    public void updateWidgetSize(int appWidgetId, int widthDp, int heightDp) {
        var options = new Bundle();
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp);
        mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options);

        Executors.newSingleThreadExecutor().execute(() -> {
            var item = new WidgetItem(appWidgetId, 0, widthDp, heightDp);
            mWidgetDao.update(item);
        });
    }

    public LiveData<List<WidgetItem>> getWidgets() {
        return mWidgetDao.getAllLive();
    }

    public int snapToCellDp(int dp) {
        var cellWithPadding = CELL_DP + CELL_PADDING_DP;
        var snapped = ((dp + cellWithPadding / 2) / cellWithPadding) * cellWithPadding;
        return Math.max(cellWithPadding, snapped);
    }

    private void confirmWidgetSelection(AppWidgetProviderInfo info) {
        var appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        var success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.getProfile(), info.provider, null);

        if (success) {
            startWidgetConfiguration(info, appWidgetId);
        } else {
            var intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.getProfile());
            if (mBindLauncher != null) mBindLauncher.launch(intent);
        }
    }

    public View getWidgetView(int appWidgetId) {
        var info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (info == null) {
            return null;
        }

        var hostView = mAppWidgetHost.createView(mActivity, appWidgetId, info);
        hostView.setAppWidget(appWidgetId, info);
        return hostView;
    }

    public void deleteWidget(int appWidgetId) {
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        Executors.newSingleThreadExecutor().execute(() -> {
            // Mock item so we can delete it, we only need the ID
            // TODO: Make this only need the ID, if possible ;)
            var item = new WidgetItem(appWidgetId, 0, 0, 0);
            mWidgetDao.delete(item);
        });
    }

    public void wipeWidgets() {
        Executors.newSingleThreadExecutor().execute(mWidgetDao::wipe);
    }

    public void startListening() {
        mAppWidgetHost.startListening();
    }

    public void stopListening() {
        mAppWidgetHost.stopListening();
    }

    public void setConfigLauncher(ActivityResultLauncher<Intent> configLauncher) {
        mConfigLauncher = configLauncher;
    }

    public void setBindLauncher(ActivityResultLauncher<Intent> bindLauncher) {
        mBindLauncher = bindLauncher;
    }

    public void startWidgetConfiguration(AppWidgetProviderInfo info, int appWidgetId) {
        mPendingWidgetId = appWidgetId;
        if (info.configure != null) {
            var intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(info.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            try {
                if (mConfigLauncher != null) {
                    mConfigLauncher.launch(intent);
                }
            } catch (Exception e) {
                saveNewWidget(appWidgetId);
            }
        } else {
            saveNewWidget(appWidgetId);
        }
    }

    public void handleConfigureResult(int resultCode, @Nullable Intent data) {
        var id = (data != null) ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mPendingWidgetId) : mPendingWidgetId;
        if (resultCode == Activity.RESULT_OK && id != AppWidgetManager.INVALID_APPWIDGET_ID) {
            saveNewWidget(id);
        } else if (resultCode == Activity.RESULT_CANCELED && id == mPendingWidgetId) {
            mAppWidgetHost.deleteAppWidgetId(id);
        }
        mPendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void saveNewWidget(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            mWidgetDao.insert(new WidgetItem(id, 0, -1, -1));
        });
    }
}