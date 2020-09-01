import threading
import time

import cv2
import imutils
import numpy as np
from scipy.cluster.hierarchy import linkage, dendrogram
from matplotlib import pyplot as plt

INF = 999999

def show_image(img,title):
    resized = imutils.resize(img, height=850)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def show_connections(image,groups):
    image_copy = image.copy()

    font = cv2.FONT_HERSHEY_SIMPLEX
    fontScale = 1
    fontColor = (255, 255, 255)
    lineType = 2

    k=0
    for _,line in groups:
        for j,word in enumerate(line):
            for i in range(len(word)):
                cv2.rectangle(image_copy, word[i][3][0], word[i][3][1], (0, 255, 0), 2)
                cv2.putText(image_copy, str(k),
                            word[i][0],
                            font,
                            fontScale,
                            fontColor,
                            lineType)
                k += 1
                if i > 0:
                    cv2.line(image_copy, word[i][0], word[i-1][0], (0, 0, 255), 3)
            if j>0:
                cv2.line(image_copy, line[j-1][-1][0], line[j][0][0], (255, 0, 0), 3)

    cv2.imwrite("grupare.jpg",image_copy)


    show_image(image_copy, "TextConnected")

def area(a,b):
    a_xmax = a[1][0]
    a_xmin = a[0][0]
    a_ymax = a[1][1]
    a_ymin = a[0][1]

    b_xmax = b[1][0]
    b_xmin = b[0][0]
    b_ymax = b[1][1]
    b_ymin = b[0][1]

    dx = min(a_xmax, b_xmax) - max(a_xmin, b_xmin)
    dy = min(a_ymax, b_ymax) - max(a_ymin, b_ymin)
    if (dx>=0) and (dy>=0):
        return dx*dy
    else:
        return 0

def make_clustering(image,
                    bboxes,
                    predictions,
                    angle_variation,
                    letters_distance_treshold,
                    word_distance_treshold,
                    show):
    start_counter = time.perf_counter()

    eliminated_during_clasification = list()
    eliminated_during_clustering = list()

    area_predictions = list()
    for i,box in enumerate(bboxes):
        if predictions[i]!="NaT":
            center = ((box[0][0]+box[1][0])//2,(box[0][1]+box[1][1])//2)
            h = box[1][1]-box[0][1]
            l = box[1][0]-box[0][0]
            area_predictions.append([center,h,l,box,predictions[i]])
        else:
            eliminated_during_clasification.append(box)

    area_predictions.sort(key=lambda x: (x[0][0], x[0][1]))

    nr_points = len(area_predictions)
    distance_matrix = np.zeros((nr_points,nr_points))
    eliminate = list()
    for i in range(nr_points):
        distance_matrix[i][i] = INF
        for j in range(i):
            distance = np.sqrt((area_predictions[i][0][0]-area_predictions[j][0][0])**2 +(area_predictions[i][0][1]-area_predictions[j][0][1])**2)
            distance_matrix[i][j] = distance_matrix[j][i] = distance
            if distance - (area_predictions[i][2] + area_predictions[j][2]) <= 0:
                overlpping_ar = area(area_predictions[i][3],area_predictions[j][3])
                ar_i = area_predictions[i][1] * area_predictions[i][2]
                ar_j = area_predictions[j][1] * area_predictions[j][2]
                if ar_i < ar_j:
                    if overlpping_ar == ar_i:
                        eliminate.append(i)
                else:
                    if overlpping_ar == ar_j:
                        eliminate.append(j)

    for j in eliminate:
        for i in range(nr_points):
            distance_matrix[i][j] = INF
            distance_matrix[j][i] = INF


    area_predictions_checked = [False for _ in range(nr_points)]

    groups = list()
    for i in range(len(area_predictions)):
        j = i
        angle = 0
        word_group = list()
        line_group = list()
        more_than_one = False
        x_min = INF
        y_min = INF
        x_max = -INF
        y_max = -INF
        while area_predictions_checked[j] == False:
            word_group.append(area_predictions[j])
            k = np.argmin(distance_matrix[j])
            dist = min(distance_matrix[j])
            distance_matrix[j][k] = distance_matrix[k][j] = INF
            area_predictions_checked[j]=True
            y = area_predictions[k][0][1]-area_predictions[j][0][1]
            x = area_predictions[k][0][0]-area_predictions[j][0][0]

            if x == 0:
                x = 0.000000001
            angle = np.rad2deg(np.arctan(y/x)) - angle

            x_min = min(x_min, area_predictions[j][3][0][0])
            y_min = min(y_min, area_predictions[j][3][0][1])
            x_max = max(x_max, area_predictions[j][3][1][0])
            y_max = max(y_max, area_predictions[j][3][1][1])

            if angle_variation[0] <= angle <= angle_variation[1] \
                    and dist <= word_distance_treshold * area_predictions[j][1] \
                    and area_predictions[k][1] / 2 <= area_predictions[j][1] <= area_predictions[k][1] * 2 :

                more_than_one = True
                if dist >= letters_distance_treshold * area_predictions[j][1]:
                    line_group.append(word_group)
                    word_group = list()

                j=k
            else:
                line_group.append(word_group)
                break
        if more_than_one:
            groups.append([[(x_min,y_min),(x_max,y_max)],line_group])
        else:
            eliminated_during_clustering.append(area_predictions[j][3])

    if ("grouping" in show) or ("all" in show):
        show_connections(image,groups)

    return groups,time.perf_counter()-start_counter,eliminated_during_clasification,eliminated_during_clustering
