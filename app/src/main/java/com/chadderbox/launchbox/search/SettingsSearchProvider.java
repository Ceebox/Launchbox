package com.chadderbox.launchbox.search;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.utils.CancellationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class SettingsSearchProvider implements ISearchProvider {

    private final Context mContext;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    public SettingsSearchProvider(Context context) {
        mContext = context;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback, CancellationToken cancellationToken) {
        mExecutor.execute(() -> {
            var results = new ArrayList<ListItem>();

            try {
                var packageManager = mContext.getPackageManager();
                var intent = new Intent(Intent.ACTION_MAIN);
                intent.setPackage("com.android.settings");
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                var activities = packageManager.queryIntentActivities(intent, 0);
                var searchQuery = query.toLowerCase(Locale.getDefault());
                for (var info : activities) {

                    if (cancellationToken.isCancelled()) {
                        return;
                    }

                    var label = info.loadLabel(packageManager).toString();
                    if (label.toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                        var launchIntent = new Intent();
                        launchIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        results.add(new SettingItem(label, launchIntent));
                    }
                }
            } catch (Exception ignored) { }

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(results));
        });
    }
}
