"""from tensorflow.contrib import lite
converter = lite.TFLiteConverter.from_keras_model_file('keras_model.h5')
tfmodel = converter.convert()
open("model.tflite","wb").write(tfmodel)"""

import tensorflow as tf

model = tf.keras.models.load_model('keras_model.h5')
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open("model.tflite", "wb").write(tflite_model)