import google.generativeai as genai
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv
import os


load_dotenv()


gemini_api_key = os.getenv('GEMINI_API_KEY')

app = Flask(__name__)
CORS(app)


genai.configure(api_key=gemini_api_key)

@app.route('/query_gemini', methods=['POST'])
def query_gemini():
    try:
        data = request.json
        message = data.get('input', '')

        model = genai.GenerativeModel('gemini-1.5-flash')

        chat = model.start_chat(history=[])

        response = chat.send_message(message)

        response_text = response.text if hasattr(response, 'text') else "No response text found."

        return jsonify({"response": response_text})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(port=5000)
