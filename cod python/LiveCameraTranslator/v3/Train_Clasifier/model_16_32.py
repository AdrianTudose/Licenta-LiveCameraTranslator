from keras import Sequential, Model, Input
from keras.layers import Conv2D, MaxPool2D, Softmax, Flatten, Dense

WIDTH = 16
HEIGHT = 32
STRIDE = 4

input = Input(shape=(32,32,1))

x = Conv2D(32, 7, activation='relu')(input)
x = Conv2D(64, 5, activation='relu')(x)

x = MaxPool2D(pool_size=2)(x)

x = Conv2D(64, 3, activation='relu')(x)
x = Conv2D(64, 3, activation='relu')(x)

x = MaxPool2D(pool_size=2)(x)

x = Flatten()(x)

x = Dense(256,activation="relu")(x)

x = Dense(128,activation="relu")(x)

output = Dense(63, activation="softmax")(x)

model = Model(inputs=input, outputs=output)

model.summary()

model.compile(loss="mean_squared_error",optimizer="adam",metrics=["mse"])
