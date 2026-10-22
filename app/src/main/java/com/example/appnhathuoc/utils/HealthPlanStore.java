package com.example.appnhathuoc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class HealthPlanStore {

    private static final String PREFS_NAME = "saved_health_plans";
    private static final String KEY_RECORDS = "records";

    private HealthPlanStore() {}

    public static final class Record {
        private final String title;
        private final String content;
        private final long createdAt;

        private Record(String title, String content, long createdAt) {
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getFormattedDate() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
                    .format(new Date(createdAt));
        }
    }

    public static void save(Context context, String title, String content) throws JSONException {
        SharedPreferences preferences = preferences(context);
        JSONArray records = new JSONArray(preferences.getString(KEY_RECORDS, "[]"));
        JSONObject record = new JSONObject();
        record.put("title", title);
        record.put("content", content);
        record.put("createdAt", System.currentTimeMillis());
        records.put(0, record);
        preferences.edit().putString(KEY_RECORDS, records.toString()).apply();
    }

    public static List<Record> getAll(Context context) throws JSONException {
        JSONArray records = new JSONArray(preferences(context).getString(KEY_RECORDS, "[]"));
        List<Record> result = new ArrayList<>(records.length());
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            result.add(new Record(
                    record.getString("title"),
                    record.getString("content"),
                    record.getLong("createdAt")));
        }
        return result;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
