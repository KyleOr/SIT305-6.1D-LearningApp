package com.example.learning;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InterestSelectionActivity extends AppCompatActivity {

    private static final int MAX_SELECTION = 10;
    private static final String TAG = "InterestSelection";
    private final Set<String> selectedTopics = new HashSet<>();
    private final OkHttpClient client = new OkHttpClient();
    private FlexboxLayout container;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_selection);

        container = findViewById(R.id.topicsContainer);
        nextButton = findViewById(R.id.nextButton);

        fetchTopicsFromBackend();

        nextButton.setOnClickListener(v -> {
            if (!selectedTopics.isEmpty()) {
                UserSession.interests = new HashSet<>(selectedTopics);
                Log.d(TAG, "Selected topics: " + UserSession.interests.toString());
            }
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void fetchTopicsFromBackend() {
        // Add timestamp to prevent caching
        String url = "http://10.0.2.2:5000/getTopics?t=" + System.currentTimeMillis();
        Log.d(TAG, "Fetching topics from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error", e);
                runOnUiThread(() -> {
                    Toast.makeText(InterestSelectionActivity.this,
                            "Failed to fetch topics. Check your connection.",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseData = response.body().string();
                    Log.d(TAG, "Raw API response: " + responseData);

                    JSONObject json = new JSONObject(responseData);
                    JSONArray topicsArray = json.getJSONArray("topics");
                    ArrayList<String> topics = new ArrayList<>();

                    for (int i = 0; i < topicsArray.length(); i++) {
                        try {
                            // First try to parse as an object with "name" field
                            JSONObject topicObj = topicsArray.getJSONObject(i);
                            topics.add(topicObj.getString("name"));
                        } catch (JSONException e) {
                            // If that fails, try to parse as a plain string
                            topics.add(topicsArray.getString(i));
                        }
                    }

                    Log.d(TAG, "Processed topics: " + topics.toString());

                    runOnUiThread(() -> {
                        if (topics.isEmpty()) {
                            Toast.makeText(InterestSelectionActivity.this,
                                    "No topics available from server",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            createTopicChips(topics);
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    runOnUiThread(() ->
                            Toast.makeText(InterestSelectionActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                } finally {
                    response.close();
                }
            }
        });
    }

    private void createTopicChips(ArrayList<String> topics) {
        container.removeAllViews(); // Clear any existing views

        for (String topic : topics) {
            Button chip = new Button(this);
            chip.setText(topic);
            chip.setTextSize(16f);
            chip.setTypeface(null, Typeface.BOLD);
            chip.setAllCaps(false);
            chip.setPadding(32, 16, 32, 16);
            chip.setMinWidth(120);
            chip.setMinHeight(60);

            // Styling
            chip.setBackgroundResource(android.R.drawable.btn_default);
            chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(12, 12, 12, 12);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                if (selectedTopics.contains(topic)) {
                    // Deselect
                    selectedTopics.remove(topic);
                    chip.setBackgroundResource(android.R.drawable.btn_default);
                    chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                } else if (selectedTopics.size() < MAX_SELECTION) {
                    // Select
                    selectedTopics.add(topic);
                    chip.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_color));
                    chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                } else {
                    Toast.makeText(this,
                            "Maximum " + MAX_SELECTION + " topics allowed",
                            Toast.LENGTH_SHORT).show();
                }
            });

            container.addView(chip);
        }
    }
}