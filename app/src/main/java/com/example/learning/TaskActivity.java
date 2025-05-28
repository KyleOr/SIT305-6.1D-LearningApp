package com.example.learning;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;


import java.io.IOException;

public class TaskActivity extends AppCompatActivity {

    private static final String TAG = "TaskActivity";
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Display greeting
        TextView userNameTextView = findViewById(R.id.userName);
        userNameTextView.setText("Hello, " + UserSession.username);

        // Show task count
        TextView notificationTextView = findViewById(R.id.notification);
        notificationTextView.setText("You have " + UserSession.interests.size() + " tasks due");

        // Set up RecyclerView
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);

        // Fetch & filter tasks
        fetchTasksForUser();
    }

    private void fetchTasksForUser() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        Log.d(TAG, "Fetching tasks from API...");

        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/getTopics")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Failed to fetch topics. Check your connection.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server returned error: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(TaskActivity.this,
                                "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API Response: " + responseBody);

                    JSONObject json = new JSONObject(responseBody);
                    JSONArray topics = json.getJSONArray("topics");
                    Log.d(TAG, "Received " + topics.length() + " topics from API");

                    ArrayList<Task> newTaskList = new ArrayList<>();
                    Log.d(TAG, "User interests: " + UserSession.interests.toString());

                    for (int i = 0; i < topics.length(); i++) {
                        JSONObject topic = topics.getJSONObject(i); // Get as JSONObject
                        String name = topic.getString("name");
                        String description = topic.getString("description");

                        Log.d(TAG, "Checking topic: " + name + " - in interests: " +
                                UserSession.interests.contains(name));

                        if (UserSession.interests.contains(name)) {
                            newTaskList.add(new Task(name, description));
                            Log.d(TAG, "Added task: " + name);
                        }
                    }

                    Log.d(TAG, "Filtered tasks count: " + newTaskList.size());

                    runOnUiThread(() -> {
                        taskList.clear();
                        taskList.addAll(newTaskList);
                        taskAdapter.updateTasks(taskList);

                        if (taskList.isEmpty()) {
                            Toast.makeText(TaskActivity.this,
                                    "No tasks match your selected interests", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TaskActivity.this,
                                    "Displaying " + taskList.size() + " of " + UserSession.interests.size() + " selected tasks",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    runOnUiThread(() -> {
                        Toast.makeText(TaskActivity.this,
                                "Error processing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}