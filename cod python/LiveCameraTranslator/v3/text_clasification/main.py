import time

import cv2
import imutils
import numpy as np

from v3.Train_Clasifier.model_16_32 import model

def show_image(img,title):
    resized = imutils.resize(img, height=100)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def to_char(x):
    if x == 62:
        return "_"
    if x in range(0, 10):
        return str(x)
    if x in range(10, 36):
        return chr(x - 10 + 65)
    if x in range(36, 63):
        return chr(x - 36 + 97)
    exit(-1)

def to_string(prediction):
    m = max(prediction)
    letter = to_char(np.argmax(prediction))
    if letter != "_" and m > LETTER_CONFIDENCE_TRESHOLD:
        return [letter,m]
    return ["_",m]

def clasify_image(model, image):
    p = model.predict(np.array([np.expand_dims(np.asarray(image) / 255, axis=2)]),32)[0]
    return to_string(p)


def get_predictions(images,
                    letter_confidence_treshold):
    start_counter = time.perf_counter()

    global LETTER_CONFIDENCE_TRESHOLD
    LETTER_CONFIDENCE_TRESHOLD = letter_confidence_treshold

    start_counter2 = time.perf_counter()
    model.load_weights("Train_Clasifier/model_weights.h5")
    model_initialization_time = time.perf_counter() - start_counter2

    predictions = list()

    for img_nr,image in enumerate(images):

        printed_image = cv2.resize(image,(32,32),interpolation=cv2.INTER_NEAREST)
        printed_image = cv2.bitwise_not(printed_image)
        prediction = clasify_image(model,printed_image)

        predictions.append(prediction)

    return predictions,time.perf_counter() - start_counter - model_initialization_time