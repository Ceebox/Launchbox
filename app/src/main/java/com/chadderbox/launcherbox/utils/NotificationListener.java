package com.chadderbox.launcherbox.utils;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public final class NotificationListener extends NotificationListenerService {

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}