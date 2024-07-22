from __future__ import division, print_function
from flask import Flask, render_template, request, jsonify
import numpy as np
import tensorflow as tf
from flask_cors import CORS
from gevent.pywsgi import WSGIServer
import json

app = Flask(__name__)
CORS(app)

# Load the TensorFlow Lite model
model_path = "plant-disease-model.tflite"
interpreter = tf.lite.Interpreter(model_path=model_path)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()


labels = ['Apple Apple scab',
 'Apple Black rot',
 'Apple Cedar apple rust',
 'Apple healthy',
 'Bacterial leaf blight in rice leaf',
 'Blight in corn Leaf',
 'Blueberry healthy',
 'Brown spot in rice leaf',
 'Cercospora leaf spot',
 'Cherry (including sour) Powdery mildew',
 'Cherry (including_sour) healthy',
 'Common Rust in corn Leaf',
 'Corn (maize) healthy',
 'Garlic',
 'Grape Black rot',
 'Grape Esca Black Measles',
 'Grape Leaf blight Isariopsis Leaf Spot',
 'Grape healthy',
 'Gray Leaf Spot in corn Leaf',
 'Leaf smut in rice leaf',
 'Orange Haunglongbing Citrus greening',
 'Peach healthy',
 'Pepper bell Bacterial spot',
 'Pepper bell healthy',
 'Potato Early blight',
 'Potato Late blight',
 'Potato healthy',
 'Raspberry healthy',
 'Soybean healthy',
 'Strawberry Leaf scorch',
 'Strawberry healthy',
 'Tomato Bacterial spot',
 'Tomato Early blight',
 'Tomato Late blight',
 'Tomato Leaf Mold',
 'Tomato Septoria leaf spot',
 'Tomato Spider mites Two spotted spider mite',
 'Tomato Target Spot',
 'Tomato Tomato mosaic virus',
 'Tomato healthy',
 'algal leaf in tea',
 'anthracnose in tea',
 'bird eye spot in tea',
 'brown blight in tea',
 'cabbage looper',
 'corn crop',
 'ginger',
 'healthy tea leaf',
 'lemon canker',
 'potato crop',
 'potato hollow heart',
 'red leaf spot in tea']

def process_image(img_path):
    img = tf.keras.preprocessing.image.load_img(img_path, target_size=(256, 256))
    img_array = tf.keras.preprocessing.image.img_to_array(img)
    img_array = img_array.reshape(-1,256,256,3)
    # img_array = np.array(img_array, dtype=np.float32) / 225.0
    return img_array


@app.route('/')
def index():
    return render_template('crop.html')

@app.route('/predict', methods=['POST'])
def predict():
    try:
        if 'file' not in request.files:
            print('Received POST request to /predict')
            print('Request data:', request.data)
            print('Request files:', request.files)
            return jsonify({'error': 'No file part'})

        file = request.files['file']

        if file.filename == '':
            print('No selected file')
            return jsonify({'error': 'No selected file'})

        if file:
            img_path = 'uploads/input.jpg'
            file.save(img_path)

            img_array = process_image(img_path)

            # Input tensor
            interpreter.set_tensor(input_details[0]['index'], img_array)

            # Perform inference
            interpreter.invoke()

            # Output tensor
            predictions = interpreter.get_tensor(output_details[0]['index'])
            
            index = np.argmax(predictions, axis=1)
            # Assuming predictions is a list of class probabilities

            disease = labels[index[0]]
            disease1 = disease.lower()
            f = open('cure.json',errors="ignore")
            data = json.load(f)
            

            if disease1.find('healthy') == -1:
                x = data[disease1].split('.')
            else:
                x = 1
            f.close()

            return jsonify({'result': disease, 'cure': x})
    except Exception as e:
        print(f'Error in predict function: {str(e)}')
        return jsonify({'error': str(e)})


if __name__ == '__main__':
    app.run(debug=True)