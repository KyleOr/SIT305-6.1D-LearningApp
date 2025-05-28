package com.example.learning;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    private TextView resultsTextView;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsTextView = findViewById(R.id.resultsTextView);
        continueButton = findViewById(R.id.continueButton);

        // Retrieve the quiz results passed from the QuestionActivity
        ArrayList<Question> questions = (ArrayList<Question>) getIntent().getSerializableExtra("questions");
        ArrayList<String> userAnswers = getIntent().getStringArrayListExtra("userAnswers");

        // Display the results
        StringBuilder results = new StringBuilder("Your Results:\n\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String userAnswer = userAnswers.get(i);
            results.append("Q" + (i + 1) + ": " + q.text + "\n");
            results.append("Your Answer: " + userAnswer + "\n");
            results.append("Correct Answer: " + q.correct + "\n\n");
        }

        resultsTextView.setText(results.toString());

        continueButton.setOnClickListener(v -> {
            // Go back to TaskActivity
            Intent intent = new Intent(ResultsActivity.this, TaskActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
