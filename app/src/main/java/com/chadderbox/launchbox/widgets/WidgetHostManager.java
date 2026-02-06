package com.chadderbox.launchbox.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.dialogs.CommandService;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.ui.components.ResizableWidgetFrame;
import com.chadderbox.launchbox.widgets.commands.ConfigureWidgetCommand;
import com.chadderbox.launchbox.widgets.commands.RemoveWidgetCommand;
import com.chadderbox.launchbox.widgets.data.WidgetDao;
import com.chadderbox.launchbox.widgets.data.WidgetDatabase;
import com.chadderbox.launchbox.widgets.data.WidgetItem;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public final class WidgetHostManager {

    private static final int APPWIDGET_HOST_ID = 1024;
    private static final int CELL_DP = 70;
    private static final int CELL_PADDING_DP = 2;

    private final Activity mActivity;
    private final AppWidgetManager mAppWidgetManager;
    private final AppWidgetHost mAppWidgetHost;
    private final WidgetDao mWidgetDao;
    private final ViewGroup mContainer;
    private ActivityResultLauncher<Intent> mConfigLauncher;
    private ActivityResultLauncher<Intent> mBindLauncher;
    private int mPendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public WidgetHostManager(Activity activity, ViewGroup container) {
        mActivity = activity;
        mContainer = container;
        mAppWidgetManager = AppWidgetManager.getInstance(activity);
        mAppWidgetHost = new AppWidgetHost(activity, APPWIDGET_HOST_ID);

        mWidgetDao = WidgetDatabase.getDatabase(mActivity).widgetDao();
    }

    public void setConfigLauncher(ActivityResultLauncher<Intent> launcher) {
        mConfigLauncher = launcher;
    }

    public void setBindLauncher(ActivityResultLauncher<Intent> launcher) {
        mBindLauncher = launcher;
    }

    public void loadSavedWidgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            var savedWidgets = mWidgetDao.getAll();
            mActivity.runOnUiThread(() -> {
                for (var item : savedWidgets) {
                    restoreWidget(item);
                }
            });
        });
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

    public WidgetDao getWidgetDao() {
        return mWidgetDao;
    }

    public void startListening() {
        mAppWidgetHost.startListening();
    }

    public void stopListening() {
        mAppWidgetHost.stopListening();
    }

    public void setWidgetsResizing(final boolean editing) {
        // TODO: Maybe some more granular control here
        for (var i = 0; i < mContainer.getChildCount(); i++) {
            var child = mContainer.getChildAt(i);
            if (child instanceof ResizableWidgetFrame frame) {
                frame.setEditing(editing);
            }
        }
    }

    public void startWidgetConfiguration(AppWidgetProviderInfo info, int appWidgetId) {
        if (!isWidgetAlreadyAdded(appWidgetId)) {
            mPendingWidgetId = appWidgetId;
        }

        if (info.configure != null) {
            var intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(info.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            // Catch permission denial if the provider's config activity is not exported
            try {
                if (mConfigLauncher != null) {
                    mConfigLauncher.launch(intent);
                }
            } catch (SecurityException | ActivityNotFoundException e) {
                // Configuration option not available, just place the widget
                processNewWidget(appWidgetId);
            }
        } else {
            processNewWidget(appWidgetId);
        }
    }

    public void createWidgetView(int appWidgetId, int widthDp, int heightDp) {
        var appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) {
            return;
        }

        var metrics = mActivity.getResources().getDisplayMetrics();
        var density = metrics.density;

        var containerWidth = mContainer.getWidth() > 0 ? mContainer.getWidth() : metrics.widthPixels;
        var containerHeight = mContainer.getHeight() > 0 ? mContainer.getHeight() : metrics.heightPixels;

        var availableWidthDp = (int) ((containerWidth - mContainer.getPaddingLeft() - mContainer.getPaddingRight()) / density);
        var availableHeightDp = (int) ((containerHeight - mContainer.getPaddingTop() - mContainer.getPaddingBottom()) / density);

        var hostView = mAppWidgetHost.createView(mActivity, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);

        var resizableFrame = new ResizableWidgetFrame(mActivity);
        resizableFrame.setTag(appWidgetId);

        var targetWidth = widthDp > 0 ? widthDp : appWidgetInfo.minWidth;
        var targetHeight = heightDp > 0 ? heightDp : appWidgetInfo.minHeight;

        var finalWidthDp = Math.max(CELL_DP, Math.min(availableWidthDp, snapToCellDp(targetWidth)));
        var finalHeightDp = Math.max(CELL_DP, Math.min(availableHeightDp, snapToCellDp(targetHeight)));

        var frameParams = new ViewGroup.LayoutParams(
            (int) (finalWidthDp * density),
            (int) (finalHeightDp * density)
        );

        resizableFrame.setLayoutParams(frameParams);
        resizableFrame.addView(hostView, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        updateWidgetSizeOptions(appWidgetId, finalWidthDp, finalHeightDp);

        resizableFrame.setOnResizeListener((width, height) -> {
            var newWidthDp = Math.min(availableWidthDp, (int) (width / density));
            var newHeightDp = Math.min(availableHeightDp, (int) (height / density));

            updateWidgetSizeOptions(appWidgetId, newWidthDp, newHeightDp);

            Executors.newSingleThreadExecutor().execute(() -> {
                var item = new WidgetItem(appWidgetId, 0, newWidthDp, newHeightDp);
                mWidgetDao.update(item);
            });
        });

        resizableFrame.setOnLongClickListener(v -> {
            var commands = new ArrayList<IDialogCommand>();
            if (canConfigureWidget(appWidgetInfo)) {
                commands.add(new ConfigureWidgetCommand(this, appWidgetInfo, appWidgetId));
            }

//            commands.add(new ResizeWidgetCommand(this, resizableFrame, appWidgetId));
            commands.add(new RemoveWidgetCommand(this, resizableFrame, appWidgetId));
            ServiceManager.getService(CommandService.class).showCommandDialog(
                "Edit Widget",
                commands
            );

            return true;
        });

        mContainer.addView(resizableFrame);
    }

    public void removeWidget(ResizableWidgetFrame frame, int appWidgetId) {
        mContainer.removeView(frame);
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        Executors.newSingleThreadExecutor().execute(() -> {
            var item = new WidgetItem(appWidgetId, 0, 0, 0);
            mWidgetDao.delete(item);
        });
    }

    public void wipeWidgets() {
        var widgetIds = new int[mContainer.getChildCount()];
        for (var i = 0; i < mContainer.getChildCount(); i++) {
            var child = mContainer.getChildAt(i);
            var tag = child.getTag();

            if (tag instanceof Integer id) {
                widgetIds[i] = id;
            } else {
                widgetIds[i] = AppWidgetManager.INVALID_APPWIDGET_ID;
            }
        }

        mActivity.runOnUiThread(mContainer::removeAllViews);
        Executors.newSingleThreadExecutor().execute(() -> {
            for (var id : widgetIds) {
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    mAppWidgetHost.deleteAppWidgetId(id);
                }
            }

            mWidgetDao.wipe();
        });
    }

    public void handleConfigureResult(int resultCode, @Nullable Intent data) {
        var appWidgetId = (data != null)
            ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mPendingWidgetId)
            : mPendingWidgetId;

        if (resultCode == Activity.RESULT_OK && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            processNewWidget(appWidgetId);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && appWidgetId == mPendingWidgetId) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }

        mPendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
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
            if (mBindLauncher != null) {
                mBindLauncher.launch(intent);
            }
        }
    }

    private void processNewWidget(int appWidgetId) {
        if (appWidgetId == mPendingWidgetId) {
            createAndSaveWidget(appWidgetId);
            mPendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        }
    }

    private boolean isWidgetAlreadyAdded(int appWidgetId) {
        for (var i = 0; i < mContainer.getChildCount(); i++) {
            var tag = mContainer.getChildAt(i).getTag();
            if (tag instanceof Integer id && id == appWidgetId) {
                return true;
            }
        }
        return false;
    }

    private void configureWidget(Intent data, ActivityResultLauncher<Intent> configLauncher) {
        var extras = data.getExtras();
        if (extras == null) {
            return;
        }

        var appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        var appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidgetInfo.configure != null) {
            var intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            try {
                configLauncher.launch(intent);
            } catch (SecurityException | ActivityNotFoundException e) {
                processNewWidget(appWidgetId);
            }
        } else {
            processNewWidget(appWidgetId);
        }
    }

    private void createAndSaveWidget(final int appWidgetId) {
        createWidgetView(appWidgetId, -1, -1);
        var newItem = new WidgetItem(appWidgetId, mContainer.getChildCount(), -1, -1);
        Executors.newSingleThreadExecutor().execute(() -> mWidgetDao.insert(newItem));
    }

    private void restoreWidget(WidgetItem item) {
        createWidgetView(item.appWidgetId, item.widthDp, item.heightDp);
    }

    private boolean canConfigureWidget(AppWidgetProviderInfo info) {
        if (info.configure == null) {
            return false;
        }

        var pm = mActivity.getPackageManager();
        var intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setComponent(info.configure);

        var resolveInfo = pm.resolveActivity(intent, 0);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return false;
        }

        var activityInfo = resolveInfo.activityInfo;
        if (!activityInfo.exported && !activityInfo.packageName.equals(mActivity.getPackageName())) {
            return false;
        }

        if (activityInfo.permission != null) {
            return mActivity.checkSelfPermission(activityInfo.permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    private void updateWidgetSizeOptions(int appWidgetId, int widthDp, int heightDp) {
        var options = new Bundle();
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp);
        mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options);
    }

    private int snapToCellDp(int dp) {
        var cellWithPadding = CELL_DP + CELL_PADDING_DP;
        var snapped = ((dp + cellWithPadding / 2) / cellWithPadding) * cellWithPadding;
        return Math.max(cellWithPadding, snapped);
    }
}