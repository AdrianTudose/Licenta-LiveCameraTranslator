import os
import pickle

import numpy as np

from v3.Train_Clasifier.model_16_32 import model

name = "images_and_lables_16_32"

input = list()
output = list()
for file in os.listdir("extracts"):
    if name in file:
        handle = open(os.path.join("extracts", file), 'rb')
        input_image,input_lable = pickle.load(handle)

        input += input_image
        output += input_lable

for i in input:
    if i.shape != (32,32,1):
        print(i.shape)

model.fit(np.array(input),np.squeeze(np.array(output)), epochs=3)

model.save_weights('model_weights.h5')
model.save("keras_model.h5")