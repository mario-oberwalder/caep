/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

public class Main {
    static {System.load(
            "C:\\opencv\\build\\java\\x64\\opencv_java412.dll");}

    public static void main(String[] args) {
       FaceRecognition faceRecognition = new FaceRecognition();
       faceRecognition.startRecognition();
        //faceRecognition.playback();

    }
}
