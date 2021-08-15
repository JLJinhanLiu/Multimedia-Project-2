# CMPT 365 Multimedia Project 2
This is a GitHub repo for a individual class project for CMPT 365 at SFU.
The goal of the Project is to design a GUI program to use DCT on .bmp images, output the quantized result and the final image.

### Audio
The program can read .wav files and compress the audio with channel coupling and linear prediction, with additional Huffman encoding before outputting as a binary file.

### Image
The program can read .bmp images and apply DCT, then output the compressed image as well as the quantized data.

## Prerequisites
Configure IntelliJ for a standard JavaFX project using JavaFX 16.

* JavaFX SDK: https://gluonhq.com/products/javafx/
* IntelliJ IDEA: https://www.jetbrains.com/idea/download/

### Installing

1. Download JavaFX SDK and pick the appropriate platform
2. Download IntelliJ IDEA Community Version and pick the appropriate platform
3. Install IntelliJ IDEA on your local machine
4. Clone this repository
5. Open JavaFX and Open Project of this repository.
6. Once opened, go to File > Project Structure.
7. Go to Project > Project SDK as "`16`"
8. Go to Libraries, select "+" icon, go to your JavaFX SDK root folder and select "`lib`" folder.

## Running the Code

1. Select Run > Edit Configuration...
2. Select Application > "+" symbol on the top left > Application
3. In VM Options textfield, write "`--module-path (JavaFX SDK Root Folder)\lib --add-modules javafx.controls,javafx.fxml`"
4. In Main Class textfield, write "`Main`"

####   on macOS:

At the root folder before Javafx and Project_2_jar, run in terminal:

java --module-path "javafx-sdk-16/lib" --add-modules javafx.controls,javafx.fxml -jar "Project_2_jar/Project 2_jar.jar"

## Author & Libraries

This project uses the Java Audio Wave Spectrum API by goxr3plus.\
https://github.com/goxr3plus/Java-Audio-Wave-Spectrum-API

