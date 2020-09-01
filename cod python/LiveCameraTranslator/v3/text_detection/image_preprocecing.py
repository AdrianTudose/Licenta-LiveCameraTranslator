import time

import cv2
import imutils


def show_image(img,title):
    resized = imutils.resize(img, width=1280)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def preprocess(image,applyBiltarealFilter,bilateralFilter_d,bilateralFilter_sigmaColor,bilateralFilter_sigmaSpace,applyGaussianBlur,show):
    execution_time = 0
    start_counter = time.perf_counter()

    #Convert image to grayscale
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    if ("grayscale" in show) or ("all" in show):
        show_image(gray,"Grayscale")

    #Apply bilateral filter
    if applyBiltarealFilter:
        gray = cv2.bilateralFilter(gray,bilateralFilter_d,bilateralFilter_sigmaColor,bilateralFilter_sigmaSpace, borderType=cv2.BORDER_CONSTANT)

    if applyGaussianBlur:
        gray = cv2.GaussianBlur(gray,(3,3),2)


    if ("withFilter" in show) or ("all" in show):
        show_image(gray,"WithFilter")

    end_counter = time.perf_counter()
    execution_time += end_counter - start_counter
    return gray,execution_time