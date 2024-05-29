# query_gemini.py
import google.generativeai as genai
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv
import os

# Load environment variables from .env file
load_dotenv()

# Access the API key
gemini_api_key = os.getenv('GEMINI_API_KEY')

app = Flask(__name__)
CORS(app)

genai.configure(api_key=gemini_api_key)

model = genai.GenerativeModel('gemini-1.5-flash-latest')

@app.route('/query_gemini', methods=['POST'])
def query_gemini():
    data = request.json
    message = data.get('input', '')

    chat = model.start_chat(history=[])
    response = chat.send_message(message)

    return jsonify({"response": response.text})


if __name__ == '__main__':
    app.run(port=5011)
