import time

import imutils as imutils
import numpy as np
from PIL import Image
import cv2

def show_image(img,title):
    resized = imutils.resize(img, width=1280)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()


def get_proposed_regions(image,
                         original_image,
                         use_otsu,
                         use_adaptive_mean,
                         use_adaptive_gaussian,
                         min_area_threshold,
                         max_area_threshold,
                         min_proportion_treshold,
                         max_proportion_treshold,
                         show):
    execution_time = 0
    start_counter = time.perf_counter()

    if use_otsu:
        _,th = cv2.threshold(image,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)
    elif use_adaptive_mean:
        th = cv2.adaptiveThreshold(image, 255, cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY, 11, 2)
    elif use_adaptive_gaussian:
        th = cv2.adaptiveThreshold(image, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)

    if ("threshold" in show) or ("all" in show):
        show_image(th,"Threshold")

    contours,_ = cv2.findContours(th,cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    if ("findContours" in show) or ("all" in show):
        image_copy = original_image.copy()
        cv2.drawContours(image_copy, contours, -1, (0, 255, 0), 3)
        cv2.imwrite("contours.jpg", image_copy)
        show_image(image_copy,"Contours")

    regions = list()
    for contour in contours:
        """rect = cv2.minAreaRect(contour)
        box = cv2.boxPoints(rect)
        box = np.int0(box)

        if cv2.contourArea(contour) > area_threshold:
            regions.append(box)"""
        if min_area_threshold <= cv2.contourArea(contour) <= max_area_threshold:
            x, y, w, h = cv2.boundingRect(contour)
            if min_proportion_treshold <= w/h <= max_proportion_treshold:
                regions.append([(x,y),(x+w,y+h)])

    if ("allBoundingBoxes" in show) or ("all" in show):
        image_copy = original_image.copy()
        for region in regions:
            cv2.rectangle(image_copy, region[0], region[1], (0, 255, 0), 2)
            #cv2.drawContours(image_copy, [region], 0, (0, 255, 0), 2)

        cv2.imwrite("boundingboxes.jpg", image_copy)
        show_image(image_copy,"PredictionBoundingBoxes")


    end_counter = time.perf_counter()
    execution_time += end_counter - start_counter
    print(execution_time)
    return regions,th,execution_time



