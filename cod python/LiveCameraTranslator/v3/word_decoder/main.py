import time

def decode(structure,letter_confidence):
    start_counter = time.perf_counter()
    #spell = SpellChecker()

    text_boxes = list()

    for boundding_box,text in structure:
        for word in text:
            actual_text = ""
            for box in word:
                actual_word = ""
                prev_char = ""
                for char,conf in box[4]:
                    if prev_char!=char and conf >= letter_confidence:
                        actual_word += char + "%.2f" % conf
                        prev_char = char

                actual_text += actual_word + " "
            text_boxes.append([boundding_box,actual_text])

    return text_boxes,time.perf_counter() - start_counter
