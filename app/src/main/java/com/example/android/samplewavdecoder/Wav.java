package com.example.android.samplewavdecoder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class Wav {
    private static Wav INSTANCE = null;

    private static final String TAG = "WavDecoder";

    // Skip initial 652 bytes of start signal (lead tone) + Ignore first 44 bytes containing meta-data information from WAV file.
    private int mSkipCount = 696;
    private int mTrimCount = 130;

    private Wav() {
    }

    public static Wav getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Wav();
        }
        return INSTANCE;
    }

    public int decodeData(Context context, String fileName) {
        Log.d(TAG, "DecodeData() called with fileName = [" + fileName + "]");
        DataInputStream inputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            InputStream inStream = context.getResources().getAssets().open(fileName);
            inputStream = new DataInputStream(inStream);


            inputStream.skip(mSkipCount);

            outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Constant.FILE_DECODED_OUTPUT, Context.MODE_PRIVATE));

            int dataByteOne;
            int secondDataByte;
            //shift count is set to 1 to remove initial "0" in bit stream.
            int countToMask = 1;
            int eightBitMask = 255;
            dataByteOne = inputStream.readUnsignedByte();

            while (inputStream.available() > mTrimCount) { // Trim end 130 bytes [ end signal]
                int shiftMask, modifiedSecondByte;

                // Shift the bits in 1st byte to skip non-data bits ("0" start bit and "11" last bits)
                dataByteOne = (dataByteOne << countToMask) & 0xFF;

                secondDataByte = inputStream.readUnsignedByte();
                modifiedSecondByte = secondDataByte;

                // Create mask to get the first n bits, where n is the value stored in countToMask.
                shiftMask = (eightBitMask << (8 - countToMask)) & 0xFF;
                modifiedSecondByte = (modifiedSecondByte & shiftMask);

                // The modified Second byte should be added to the end of First byte.
                modifiedSecondByte = modifiedSecondByte >>> (8 - countToMask);
                dataByteOne = (dataByteOne & 0xFF) | modifiedSecondByte;

                outputStreamWriter.write(dataByteOne);

                // Read another byte as second byte if count to skip is 8 or more.
                countToMask = countToMask + 3;
                if (countToMask > 8) {
                    countToMask = countToMask - 8;
                    dataByteOne = inputStream.readUnsignedByte();
                    continue;
                } else if (countToMask == 8) {
                    dataByteOne = inputStream.readUnsignedByte();
                    countToMask = 3;
                    continue;
                }

                dataByteOne = secondDataByte;
            }
            Log.d(TAG, "Decoding is completed");

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public String readDecodedData1(Context context) {
        StringBuilder text = new StringBuilder();
        File data = Environment.getDataDirectory();
        String currentFilePath = "//data//" + context.getPackageName() + "//files//" + Constant.FILE_DECODED_OUTPUT;
        File currentFile = new File(data, currentFilePath);

        int[] bytes = new int[1000];

        DataInputStream inputStream = null;
        try {
            inputStream = new DataInputStream(new FileInputStream(currentFile));
            for (int i = 0; i < 1000; i++) {
                bytes[i] = (byte) inputStream.readUnsignedByte();
            }
            text.append(Arrays.toString(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Arrays.toString(text.toString().getBytes());
    }

}

