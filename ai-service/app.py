"""
CounterCheck AI Microservice - Flask REST API.
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from predict import predict_image

app = Flask(__name__)
CORS(app)
app.config["MAX_CONTENT_LENGTH"] = 10 * 1024 * 1024  # 10 MB


@app.errorhandler(413)
def request_too_large(_error):
    return jsonify({"error": "Image exceeds the 10 MB upload limit."}), 413


@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        "status": "UP",
        "service": "CounterCheck AI Service",
        "model": "MobileNetV2 Transfer Learning"
    }), 200


@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files and 'file' not in request.files:
        return jsonify({
            "error": "No image file provided. Please attach a file under key 'image' or 'file'."
        }), 400

    file = request.files.get('image') or request.files.get('file')

    if file.filename == '':
        return jsonify({"error": "Empty filename provided."}), 400

    try:
        image_bytes = file.read()
        result = predict_image(image_bytes)
        return jsonify(result), 200
    except (ValueError, TypeError):
        return jsonify({"error": "The uploaded file could not be processed as a valid image."}), 400
    except Exception:
        app.logger.exception("Prediction failed")
        return jsonify({"error": "Prediction service failed to process the image."}), 500


if __name__ == '__main__':
    print("[AI-SERVICE] Starting CounterCheck AI Flask Microservice on http://localhost:5000 ...")
    app.run(host='0.0.0.0', port=5000, debug=False)
