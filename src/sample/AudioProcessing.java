package sample;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class AudioProcessing {

    public static void compress(File file){
        try
        {
            // Open the wav file
            WavFile wavFile = WavFile.openWavFile(file);

            // Display information about the wav file
            wavFile.display();

            int[][] midside = new int[2][(int)wavFile.getNumFrames()];
            int frameCounter = 0;

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of 100 frames
            int[][] buffer = new int[numChannels][100];
            int framesRead;

            do
            {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, 100);
                for (int s=0 ; s<framesRead ; s++)
                {
                    // calculate channel coupling
                    if (numChannels > 1){
                        midside[0][frameCounter] = (buffer[0][s] + buffer[1][s]) / 2;
                        midside[1][frameCounter] = buffer[0][s] - buffer[1][s];
                    }
                    else {
                        midside[0][frameCounter] = buffer[0][s];
                    }
                    frameCounter++;
                }
            }
            while (framesRead != 0);

            // Close the wavFile
            wavFile.close();

            // calculate DPCM
            for (int i = frameCounter - 1; i > 0; i--){
                midside[0][i] = midside[0][i] - midside[0][i-1];
                if (numChannels > 1){
                    midside[1][i] = midside[1][i] - midside[1][i-1];
                }
            }

            // compress file with huffman
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < frameCounter; i++){
                output.append((short) midside[0][i]).append(',').append((short) midside[1][i]);
            }
            HuffmanCoding huffmanCoding = new HuffmanCoding(output.toString());
            huffmanCoding.compress();

            // write to output
            File fileOut = new File(String.join("",file.getAbsolutePath() + "-compressed.txt"));
            fileOut.createNewFile();
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(String.join("", file.getAbsolutePath() + "-compressed.txt")));

            // write dictionary
            for (Map.Entry entry : huffmanCoding.getDictionary().entrySet()){
                dataOutputStream.writeChars(entry.getKey() + ": " + entry.getValue() + '\n');
            }

            // write encoded string in sections of bytes
            byte outByte = 0b00000000;
            int binSize = huffmanCoding.getCompressedSize();
            for (int i = 0; i < binSize; i++){
                if ((i % 8 == 0 && i != 0)){
                    dataOutputStream.writeByte(outByte);
                    outByte = 0b00000000;
                }
                if (huffmanCoding.getCompressedString().charAt(i) == '1'){
                    outByte |= (byte) Math.pow(2, 7 - i%8);
                }
                if (i > huffmanCoding.getCompressedSize() - 1){
                    dataOutputStream.writeByte(outByte);
                }
            }
            dataOutputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getRatio(File file){
        String returnString = "This original file size is: ";

        try {
            Path path = Paths.get(file.getAbsolutePath());
            long oldFileSize = Files.size(path);
            returnString = String.join("", returnString, Long.toString(oldFileSize), " bits. ");

            path = Paths.get(file.getAbsolutePath() + "-compressed.txt");
            long newFileSize = Files.size(path);
            returnString = String.join("", returnString, "The new file size is: ",
                    Long.toString(newFileSize), " bits");

            returnString = String.join("", returnString, "\nThe compression ratio is: ",
                    Double.toString((double)oldFileSize/(double)newFileSize));
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return returnString;
    }

}
