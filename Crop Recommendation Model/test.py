import pickle
import numpy as np

with open("Model/RandomForest.pkl", "rb") as f:
    crop_model = pickle.load(f)

crop_labels = ['rice', 'maize', 'chickpea', 'kidneybeans', 'pigeonpeas',
       'mothbeans', 'mungbean', 'blackgram', 'lentil', 'pomegranate',
       'banana', 'mango', 'grapes', 'watermelon', 'muskmelon', 'apple',
       'orange', 'papaya', 'coconut', 'cotton', 'jute', 'coffee']

# dataframe : [N, P,K,temperature,humidity,ph,rainfall]
data = np.array([[33,77,15,23.89736406,66.32102048,7.802212437,40.74536757]])
prediction = crop_model.predict(data)
print(prediction[0])