# gpt2_service.py
from flask import Flask, request, jsonify
from transformers import pipeline

app = Flask(__name__)

enhancer = pipeline("text-generation", model="gpt2")

@app.route('/')
def index():
    return 'Hello, World!'

@app.route('/enhance', methods=['POST'])
def enhance():
    data = request.get_json()
    input_text = data.get('input', 'java')
    result = enhancer(input_text, max_length=150, num_return_sequences=1)
    return jsonify({'response': result[0]['generated_text']})

if __name__ == '__main__':
    app.run(port=5000)
