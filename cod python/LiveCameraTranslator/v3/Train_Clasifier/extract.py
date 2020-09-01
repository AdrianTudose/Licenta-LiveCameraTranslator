import os
import pickle
import random

import numpy as np
from PIL import Image
from keras.preprocessing.image import img_to_array, array_to_img

images_and_lables = list()
total_images = 0


def resize_fct(image, size, max_grow_factor, rotation,elim_border = False):
    if elim_border:
        width,height = image.size
        image = image.crop((5,5,width-5,height-5))

    if not (rotation[0] == 0 and rotation[1] == 0):
        image = image.rotate(random.randrange(rotation[0], rotation[1]))

    if max_grow_factor:
        w = random.randrange(size[0], int(size[0] * (1 + max_grow_factor)))
    else:
        w = size[0]
    h = size[1]

    return image.resize((w, h))


def generate(image, max_grow_factor, size, nr, rotation,elim_border = False):
    images = list()

    for i in range(nr):
        new_image = resize_fct(image, size, max_grow_factor, rotation,elim_border)

        width, h = new_image.size

        w = random.randrange(0, width - size[0] + 1)

        images.append(new_image.crop((w, 0, w + size[0], h)))

        """show = random.random()
        if show < 0.0005:
            images[-1].show()"""

    return images


def extract_from(folder_path, size, per_img, good=True, max_grow_factor=0.1, rotation=(-10, 10),elim_border = False):
    print("Extracting images from", folder_path, "...")
    global total_images

    for folder in os.listdir(folder_path):
        if good:
            class_number = int(folder[-2:]) - 1
        else:
            class_number = 62

        class_lable = np.zeros((1, 1, 63))
        class_lable[0][0][class_number] = 1

        path = os.path.join(folder_path, folder)
        images_number = len(os.listdir(path))
        for i, file in enumerate(os.listdir(path)):
            total_images += 1 + per_img
            print(total_images, "class:", class_number, "image:", i, "/", images_number)

            image = Image.open(os.path.join(path, file))
            image = image.convert("L")
            generated_images = generate(image, max_grow_factor, size, per_img, rotation,elim_border)
            for img in generated_images:
                images_and_lables.append([img_to_array(img)/255, class_lable])
                images_and_lables.append([1-img_to_array(img)/255, class_lable])


def extract(name, size):
    extract_from("../../images_and_data/English/Img/GoodImg/Bmp", size, 10, good=True)
    extract_from("../../images_and_data/English/Img/BadImag/Bmp", size, 4, good=True)
    #extract_from("../../images_and_data/Img", size, 2, good=True)
    #extract_from("../../images_and_data/Fnt", size, 1, good=True,elim_border = True,rotation=(0,0))
    #extract_from("../../images_and_data/BlackWhite", size, 100, good=False, max_grow_factor=0, rotation=(0, 0))
    extract_from("../../images_and_data/natural_scenes_v2",size,1,good=False)

    print("\ndone extracting", total_images, "images.\n")

    print("shuffling...")
    random.shuffle(images_and_lables)
    print("done shuffling.\n")

    print("rearranging...")
    images, lables = list(zip(*images_and_lables))
    print("done rearranging.\n")

    print("saving...")
    nr = 1
    for i in range(1, len(images) // 20000):
        il = [images[(i - 1) * 20000:i * 20000], lables[(i - 1) * 20000:i * 20000]]
        with open('extracts/' + name + "_p" + str(nr) + ".pickle", 'wb') as handle:
            pickle.dump(il, handle, protocol=pickle.HIGHEST_PROTOCOL)
        nr += 1
    il = [images[-((len(images) % 20000)):], lables[-((len(images) % 20000)):]]
    with open('extracts/' + name + "_p" + str(nr) + ".pickle", 'wb') as handle:
        pickle.dump(il, handle, protocol=pickle.HIGHEST_PROTOCOL)
    print("done saving.\n")

    print("DONE")


extract("images_and_lables_16_32", (32, 32))
