from flask import Flask, request, jsonify
import subprocess

app = Flask(__name__)

@app.route('/extract_tags', methods=['POST'])
def extract_tags():
    data = request.json
    text = data.get('text', '')
    result = subprocess.run(['python', 'extract_tags.py', text], capture_output=True, text=True)
    if result.returncode == 0:
        tags = result.stdout.strip().split(',')
        return jsonify({"tags": tags})
    else:
        return jsonify({"error": result.stderr}), 500

if __name__ == "__main__":
    app.run(port=5000)
