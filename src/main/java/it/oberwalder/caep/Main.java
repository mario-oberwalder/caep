/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import static org.opencv.highgui.HighGui.*;


public class Main {
    static {System.load(
            "C:\\Program Files\\opencv\\build\\java\\x64\\opencv_java412.dll");}

    public static void main(String[] args) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        Mat imageArray = new Mat();
        Mat resizedImage = new Mat();
        VideoCapture videoDevice = new VideoCapture(
                "C:\\Users\\codersbay\\Desktop\\Movie\\2019_1105_171004_850.mp4");
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load(
                "C:\\Program Files\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_default.xml");
        int absoluteFaceSize = 0;

        videoDevice.open(
                "C:\\Users\\codersbay\\Desktop\\Movie\\2019_1106_085400_866.mp4");


        if (videoDevice.isOpened()) {
            namedWindow( "Wonderful window", WINDOW_AUTOSIZE );

            while(videoDevice.read(imageArray)) {
                // convert the frame in gray scale
                Imgproc.cvtColor(imageArray, grayFrame, Imgproc.COLOR_BGR2GRAY);
                // equalize the frame histogram to improve the result
                Imgproc.equalizeHist(grayFrame, grayFrame);
                // compute minimum face size (20% of the frame height, in our case)
                if (absoluteFaceSize == 0)
                {
                    int height = grayFrame.rows();
                    if (Math.round(height * 0.01f) > 0)
                    {
                        absoluteFaceSize = Math.round(height * 0.2f);
                    }
                }

                faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());

                // each rectangle in faces is a face: draw them!
                Rect[] facesArray = faces.toArray();
                for (int i = 0; i < facesArray.length; i++)
                    Imgproc.rectangle(imageArray, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

                Imgproc.resize(imageArray,resizedImage,new Size(1280 , 720));
                imshow("Wonderful window", resizedImage);
                waitKey(1);
            }
            //(videoDevice.read(imageArray);
            //System.out.println(imageArray.toString());
            videoDevice.release();
        } else {
            System.out.println("Couldn't open video device");
        }




    }
}
