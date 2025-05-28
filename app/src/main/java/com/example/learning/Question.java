package com.example.learning;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    String text;
    ArrayList<String> options;
    String correct;

    public Question(String text, ArrayList<String> options, String correct) {
        this.text = text;
        this.options = options;
        this.correct = correct;
    }
}
