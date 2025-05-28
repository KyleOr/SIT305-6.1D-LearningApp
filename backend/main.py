import os
import requests
from flask import Flask, request, jsonify
import re

app = Flask(__name__)

# Hugging Face Inference API Setup
MODEL = "mistralai/Mistral-7B-Instruct-v0.3"
API_URL = f"https://api-inference.huggingface.co/models/{MODEL}"
HF_API_TOKEN = "SECRET"
HEADERS = {"Authorization": f"Bearer {HF_API_TOKEN}"}


@app.route('/getTopics', methods=['GET'])
def get_topics():
    topics = [
        {"name": "Math", "description": "Solve various mathematical problems."},
        {"name": "Science", "description": "Explore scientific concepts and experiments."},
        {"name": "History", "description": "Learn about historical events and figures."},
        {"name": "Technology", "description": "Stay updated with the latest tech advancements."},
        {"name": "Literature", "description": "Dive into classic and modern literature."},
        {"name": "Geography", "description": "Understand the world's geography and cultures."},
        {"name": "Art", "description": "Discover various art forms and techniques."},
        {"name": "Music", "description": "Explore different genres and music theory."},
        {"name": "Health", "description": "Learn about health, nutrition, and fitness."},
        {"name": "Sports", "description": "Stay informed about various sports and athletes."}
    ]
    return jsonify({"topics": topics}), 200


def fetchQuizFromLlama(student_topic):
    print("Fetching quiz from Hugging Face API")

    prompt_text = (
        f"Generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"Format your response as follows:\n"
        f"**QUESTION 1:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 2:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 3:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"Here is the student topic:\n{student_topic}"
    )

    payload = {
        "inputs": prompt_text,
        "parameters": {
            "temperature": 0.7,
            "top_p": 0.9,
            "max_new_tokens": 500
        }
    }

    try:
        response = requests.post(API_URL, headers=HEADERS, json=payload)
        print("Status Code:", response.status_code)
        print("Response Text:", response.text)
        response.raise_for_status()
        return response.json()[0]["generated_text"]
    except Exception as e:
        print("API call failed:", e)
        raise


def process_quiz(quiz_text):
    questions = []
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n|$)',
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans = match[5].strip()

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans
        }
        questions.append(question_data)

    return questions


@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if not student_topic:
        return jsonify({'error': 'Missing topic parameter'}), 400
    try:
        quiz = fetchQuizFromLlama(student_topic)
        processed_quiz = process_quiz(quiz)
        if not processed_quiz:
            return jsonify({'error': 'Failed to parse quiz data', 'raw_response': quiz}), 500
        return jsonify({'quiz': processed_quiz}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200


if __name__ == '__main__':
    port_num = 5000
    print(f"App running on port {port_num}")
    app.run(debug=True, port=port_num, host="0.0.0.0")
