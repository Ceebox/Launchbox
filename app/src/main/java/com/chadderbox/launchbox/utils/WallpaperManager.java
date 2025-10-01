package com.chadderbox.launchbox.utils;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class WallpaperManager {

    private final ImageView mWallpaperHost;
    private Drawable mWallpaperDrawable;

    public WallpaperManager(ImageView wallpaperHost) {
        mWallpaperHost = wallpaperHost;
    }

    /** Either load in the wallpaper, or use a blank one so we can dim it. */
    public void applyBackground() {
        var wallpaperPath = SettingsManager.getWallpaper();
        Drawable loadedDrawable = null;

        if (wallpaperPath != null && !wallpaperPath.isEmpty()) {
            var wallpaperUri = Uri.parse(wallpaperPath);
            try (var inputStream = mWallpaperHost.getContext().getContentResolver().openInputStream(wallpaperUri)) {
                if (inputStream != null) {
                    loadedDrawable = Drawable.createFromStream(inputStream, wallpaperUri.toString());
                }
            } catch (Exception ignored) { }
        }

        if (loadedDrawable == null) {
            loadedDrawable = new ColorDrawable(0xFF000000);
        }

        mWallpaperDrawable = loadedDrawable;
        setWallpaperDim();
        mWallpaperHost.setImageDrawable(mWallpaperDrawable);
    }

    private void setWallpaperDim() {
        var drawable = mWallpaperDrawable.mutate();
        drawable.setColorFilter(new BlendModeColorFilter(getDimColour(SettingsManager.getWallpaperDimAmount()), BlendMode.SRC));
    }

    private int getDimColour(float dimAmount) {
        dimAmount = Math.max(0f, Math.min(dimAmount, 1f));
        var alpha = (int) (dimAmount * 255);
        return alpha << 24;
    }
}
