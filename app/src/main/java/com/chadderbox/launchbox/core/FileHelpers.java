package com.chadderbox.launchbox.core;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

public final class FileHelpers {

    private FileHelpers() { }

    public static String tryGetFileNameFromString(final Context context, final String uriPath) {
        Uri uri;
        try {
            uri = Uri.parse(uriPath);
        } catch (Exception ignored) {
            return null;
        }

        return tryGetFileNameFromUri(context, uri);
    }

    public static String tryGetFileNameFromUri(final Context context, final Uri uri) {
        String result = null;
        if (uri == null) {
            return null;
        }

        if ("content".equals(uri.getScheme())) {
            try (var cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    var nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) { }
        }

        if (result == null) {
            var path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                } else {
                    result = path;
                }
            }
        }

        return result;
    }

}
