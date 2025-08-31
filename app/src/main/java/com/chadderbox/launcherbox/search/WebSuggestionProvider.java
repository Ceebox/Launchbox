package com.chadderbox.launcherbox.search;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.data.SuggestionItem;
import com.chadderbox.launcherbox.data.WebItem;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Provides web search results + suggestions.
 */
public final class WebSuggestionProvider implements ISearchProvider {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        mExecutor.execute(() -> {
            List<ListItem> suggestions = new ArrayList<>();
            try {
                String endpoint = "https://suggestqueries.google.com/complete/search?client=firefox&q=" +
                    java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);

                var conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    var sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    var arr = new JSONArray(sb.toString());
                    var suggestionsArr = arr.getJSONArray(1);
                    for (int i = 0; i < Math.min(5, suggestionsArr.length()); i++) {
                        suggestions.add(new SuggestionItem(suggestionsArr.getString(i)));
                    }
                }
            } catch (Exception e) {
                Log.d("WebSuggestionProvider", "Error getting suggestions", e);
                // We are offline, or the api is brokey
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(suggestions));
        });
    }
}

