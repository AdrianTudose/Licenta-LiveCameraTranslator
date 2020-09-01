import threading

import cv2
import imutils


def show_image(img,title):
    resized = imutils.resize(img, height=900)
    cv2.imshow(title, resized)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def show_predictions(image,bboxes,predictions):
    copy_image = image.copy()
    font = cv2.FONT_HERSHEY_SIMPLEX
    fontScale = 1
    fontColor = (255, 255, 255)
    lineType = 2

    k=0
    for i in range(len(bboxes)):
        if predictions[i][0] != "_":
            cv2.rectangle(copy_image, bboxes[i][0], bboxes[i][1], (0, 255, 0), 2)
            cv2.putText(copy_image, "".join(predictions[i][0]),
                        bboxes[i][0],
                        font,
                        fontScale,
                        fontColor,
                        lineType)
        k+=1
    cv2.imwrite("predictions.jpg",copy_image)
    show_image(copy_image, "Predictions")

def show_eliminated(image,hierarchy,clas_elim,cluster_elim):
    word_color = (0,128,0) #verde
    text_color = (255, 255 ,0 ) #albastru
    clas_elim_color = (255, 0, 0) #rosu
    cluster_elim_color = (255, 128, 128) #mov

    image_copy = image.copy()

    font = cv2.FONT_HERSHEY_SIMPLEX
    fontScale = 1
    fontColor = (255, 255, 255)
    lineType = 2

    for box in clas_elim:
        cv2.rectangle(image_copy, box[0], box[1], clas_elim_color, 2)

    for box in cluster_elim:
        cv2.rectangle(image_copy, box[0], box[1], cluster_elim_color, 2)

    for text_box, line in hierarchy:
        cv2.rectangle(image_copy, (text_box[0][0]-4,text_box[0][1]-4), (text_box[1][0]+4,text_box[1][1]+4), text_color, 4)
        for word in line:
            x_min = min([x[3][0][0] for x in word])
            x_max = max([x[3][1][0] for x in word])
            y_min = min([x[3][0][1] for x in word])
            y_max = max([x[3][1][1] for x in word])
            print([x[[4][0]] for x in word])
            text = "".join([x[4][0][0][0] for x in word])
            cv2.rectangle(image_copy, (x_min,y_min), (x_max,y_max), word_color, 4)
            cv2.putText(image_copy, text,
                            (x_min,y_min),
                            font,
                            fontScale,
                            fontColor,
                            lineType)

    show_image(image_copy, "RecognizedAndEliminated")

def show_final_image(image,text_boxes):
    th = threading.Thread(target=show_image, args=(image,"InitialImage"))
    th.start()

    font = cv2.FONT_HERSHEY_SIMPLEX
    fontScale = 1
    fontColor = (0,0,0)
    lineType = 2

    image_copy = image.copy()
    for box,text in text_boxes:
        cv2.rectangle(image_copy, box[0], box[1], (255,255,255), cv2.FILLED)
        cv2.putText(image_copy, text,
                    (box[0][0],box[1][1]),
                    font,
                    fontScale,
                    fontColor,
                    lineType)

    show_image(image_copy,"TextReplaced")