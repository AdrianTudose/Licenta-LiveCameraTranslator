import cv2
import numpy as np

from v3.Train_Clasifier.model_16_32 import model

def show_image(img,title):
    #resized = imutils.resize(img, width=1280)
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

model.load_weights("model_weights.h5")

image = cv2.imread('../../images_and_data/testin_images/H_shop.jpg')

resized = cv2.resize(image, (32,32), interpolation = cv2.INTER_AREA)

gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)

show_image(gray,"Image")

arr = np.expand_dims(np.asarray(gray)/255,axis = 2)

prediction = model.predict(np.array([arr]))[0]

print(prediction)
print(arr.shape)
print(max(prediction))
print(to_char(np.argmax(prediction)))

