import time

import cv2
import imutils
import numpy as np

from v3.text_detection.main import get_proposed_regions
from v3.text_clasification.main import get_predictions
from v3.text_clustering.main import make_clustering
from v3.show import show_predictions, show_eliminated, show_final_image
from v3.word_decoder.main import decode

def show_image(img,title):
    resized = imutils.resize(img, height=750)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def extract_bounding_boxes(image,bboxes,padding,show):
    images = list()
    gray = image#cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    print(image.shape)
    for bbox in bboxes:
        copy_image = gray.copy()
        if bbox[0][1]>padding:
            x1 = bbox[0][1] - padding
        else:
            x1 = bbox[0][1]
        if bbox[1][1]>padding:
            x2 = bbox[1][1] - padding
        else:
            x2 = bbox[1][1]
        if bbox[0][0]>padding:
            y1 = bbox[0][0] + padding
        else:
            y1 = bbox[0][0]
        if bbox[1][0]>padding:
            y2 = bbox[1][0] + padding
        else:
            y2 = bbox[1][0]
        img_part = gray[x1:x2, y1:y2]
        images.append(img_part)

        if ("individualBoxes" in show) or ("all" in show):
            img_part = cv2.resize(img_part, (32, 32),interpolation=cv2.INTER_LINEAR_EXACT)
            img_part = cv2.resize(img_part, (32 * 10 , 32 * 10))
            img_part = cv2.copyMakeBorder(img_part, 0,copy_image.shape[0] - 32 * 10 , 0, 0, cv2.BORDER_CONSTANT)
            copy_image = np.concatenate((copy_image,img_part),axis=1)
            cv2.rectangle(copy_image, bbox[0], bbox[1], (0, 255, 0), 2)
            show_image(copy_image,"img")
    return images

def main():
    #open image
    image = cv2.imread('../images_and_data/testin_images/natural_scene.jpg')
    image = imutils.resize(image, width=700)

    pShow = show_steps = [
        "none",
        "grayscale",
        "withFilter",
        "threshold",
        "findContours",
        "allBoundingBoxes",
        #"individualBoxes",
        "predictions",
        "grouping",
        "eliminated",
        #"all"
    ]

    #Compute proposed bounding boxes

    p1 = applyBiltaralFilter = False
    p2 = bilateralFilter_d = 9
    p3 = bilateralFilter_sigmaColor = 50
    p4 = bilateralFilter_sigmaSpace = 50
    p5 = applyGaussianBlur = True
    p6 = use_otsu = False
    p7 = use_adaptive_mean = False
    p8 = use_adaptive_gaussian = True
    p9 = min_area_threshold = 100
    p10 = max_area_threshold = 700*700*0.25
    p11 = min_proportion_treshold = 1/5
    p12 = max_proportion_treshold = 2/1

    bboxes,binary_image,time1,time2 = get_proposed_regions(image,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,pShow)


    #Compute predictions for bounding boxes
    padding = 3
    image_parts = extract_bounding_boxes(binary_image,bboxes,padding,pShow)

    p1 = letter_confidence_treshold = 0.6

    predictions,time3 = get_predictions(image_parts,p1)

    if ("predictions" in pShow) or ("all" in pShow):
        show_predictions(image,bboxes,predictions)

    #Clustering into words and text

    p1 = angle_variation = (-20, +20)
    p2 = letters_distance_treshold = 1
    p3 = word_distance_treshold = 2

    text_hierchy,time4,clas_elim,cluster_elim = make_clustering(image,bboxes,predictions,p1,p2,p3,pShow)

    #Show result
    print("Total execution time:",time1+time2+time3+time4)
    print("Image preprocecing:",time1)
    print("Find BoundingBoxes:",time2)
    print("Compute predictions:",time3)
    print("Groupe boxes:",time4)

    if ("eliminated" in pShow) or ("all" in pShow):
        show_eliminated(image,text_hierchy,clas_elim,cluster_elim)

    show_final_image(image,text_boxes)


if __name__ == "__main__":
    main()
