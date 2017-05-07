# TCHAT
TCHAT (Tactile CHAT) is a device to send basic tactile gestures over the internet. The device is a glove sensors mounted on the inner lining. Piezo sensors mounted on the inner lining capture the position of pressure points applied on the hand. These cooridnates are sent to the chat client, where the correspoding pounts are "recreated" by stimulating vibrators in the glove.

This repository has the code for the companion chat application. It also includes a regular text chat and a panel to track inputs to the TCHAT device. Along with the application, we include an LSTM model which translates English characters to sets of pressure points as per the BANSZL sign language for the deafblind.

Requirements:

        Java (JRE 1.6 or later)
        Python (2.7 or later)
        NumPy
        TensorFlow
        SciPy
