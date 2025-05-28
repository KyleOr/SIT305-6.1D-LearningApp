package com.example.learning;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class QuestionActivity extends AppCompatActivity {

    private static final String TAG = "QuestionActivity";
    private String topicName;
    private TextView questionTitle, questionDescription, questionText;
    private RadioGroup optionsGroup;
    private Button submitButton, nextButton;

    private ArrayList<Question> questions = new ArrayList<>();
    private ArrayList<String> userAnswers = new ArrayList<>();
    private int currentIndex = 0;
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        topicName = getIntent().getStringExtra("taskName");
        Log.d(TAG, "Quiz for topic: " + topicName);

        questionTitle = findViewById(R.id.questionTitle);
        questionDescription = findViewById(R.id.questionDescription);
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        submitButton = findViewById(R.id.submitAnswerButton);
        nextButton = findViewById(R.id.nextQuestionButton);

        questionTitle.setText(topicName);
        questionDescription.setText("Let's test your knowledge on this topic!");

        fetchQuizData();

        submitButton.setOnClickListener(v -> {
            if (answered) return;

            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);
            String selectedText = selected.getText().toString();

            userAnswers.add(selectedText);

            Question current = questions.get(currentIndex);
            answered = true;

            for (int i = 0; i < optionsGroup.getChildCount(); i++) {
                RadioButton option = (RadioButton) optionsGroup.getChildAt(i);
                if (option.getText().toString().equals(current.correct)) {
                    option.setTextColor(Color.GREEN);
                } else if (option.getText().toString().equals(selectedText)) {
                    option.setTextColor(Color.RED);
                }
                option.setEnabled(false);
            }

            nextButton.setEnabled(true);
        });

        nextButton.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(currentIndex);
            } else {
                Intent intent = new Intent(QuestionActivity.this, ResultsActivity.class);
                intent.putExtra("questions", questions);
                intent.putStringArrayListExtra("userAnswers", userAnswers);
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchQuizData() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        String url = "http://10.0.2.2:5000/getQuiz?topic=" + topicName;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load quiz", e);
                runOnUiThread(() -> Toast.makeText(QuestionActivity.this,
                        "Failed to load quiz", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray array = json.getJSONArray("quiz");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String q = obj.getString("question");
                        String correctLetter = obj.getString("correct_answer").trim().toUpperCase();
                        JSONArray options = obj.getJSONArray("options");

                        int correctIndex = "ABCD".indexOf(correctLetter);
                        if (correctIndex < 0 || correctIndex >= options.length()) {
                            Log.e(TAG, "Invalid correct answer letter: " + correctLetter + " for question: " + q);
                            continue;
                        }

                        String correct = options.getString(correctIndex);

                        ArrayList<String> optList = new ArrayList<>();
                        for (int j = 0; j < options.length(); j++) {
                            optList.add(options.getString(j));
                        }

                        questions.add(new Question(q, optList, correct));
                    }

                    runOnUiThread(() -> showQuestion(currentIndex));

                } catch (Exception e) {
                    Log.e(TAG, "Parsing error", e);
                    runOnUiThread(() -> Toast.makeText(QuestionActivity.this,
                            "Error parsing quiz", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showQuestion(int index) {
        Question q = questions.get(index);
        questionText.setText(q.text);
        optionsGroup.removeAllViews();
        answered = false;

        for (String opt : q.options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(opt);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            optionsGroup.addView(rb);
        }

        nextButton.setEnabled(false);
        for (int i = 0; i < optionsGroup.getChildCount(); i++) {
            optionsGroup.getChildAt(i).setEnabled(true);
            ((RadioButton) optionsGroup.getChildAt(i)).setTextColor(Color.BLACK);
        }
    }
}
