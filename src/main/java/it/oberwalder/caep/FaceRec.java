/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgproc.Imgproc.rectangle;

public class FaceRec {
    private static final double CONFIDENCE_THRESHOLD = 0.35;
    private static final String NEURAL_NET_MODEL_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String NEURAL_NET_CONFIG_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\deploy.prototxt";
    private static final String TEST_MOVIE_PATH = "C:\\movies\\test2.mp4";

    public void startRec() {
    Mat imageArray = new Mat();
    Mat resizedImage = new Mat();
    Mat croppedImage = new Mat();
    Mat outImage = new Mat();

    VideoCapture videoDevice = new VideoCapture(TEST_MOVIE_PATH);
    videoDevice.open(TEST_MOVIE_PATH);
    Net net = readNetFromCaffe(NEURAL_NET_CONFIG_PATH, NEURAL_NET_MODEL_PATH);

        if (videoDevice.isOpened()) {
        namedWindow("Wonderful window", WINDOW_AUTOSIZE);

            skipFrames(imageArray, videoDevice, 4800);
            while (videoDevice.read(imageArray)) {

            Imgproc.resize(imageArray, resizedImage, new Size(450, 300));
            Rect centerCut = new Rect(75, 0, 300, 300);
            croppedImage = new Mat(resizedImage, centerCut);
            Mat blob = blobFromImage(croppedImage, 1.0, new Size(300, 300), new Scalar(104, 117, 123), false, false);

            net.setInput(blob);
            Mat detections = net.forward();
            Mat detectionMat = detections.reshape(1, (int) detections.total() / 7);
            for (int i = 0; i < detectionMat.rows(); i++) {

                double[] confidence = detectionMat.get(i, 2);
                if (confidence[0] > CONFIDENCE_THRESHOLD) {
                    int x1 = (int) ((detectionMat.get(i, 3)[0]) * 300);
                    int y1 = (int) ((detectionMat.get(i, 4)[0]) * 300);
                    int x2 = (int) ((detectionMat.get(i, 5)[0]) * 300);
                    int y2 = (int) ((detectionMat.get(i, 6)[0]) * 300);
                    rectangle(croppedImage, new Point(x2, y2), new Point(x1, y1), new Scalar(0, 174, 255), 2, 4);
                }
            }
            outImage = new Mat();
            Imgproc.resize(croppedImage, outImage,new Size(900, 900));
            imshow("Wonderful window", outImage);
            waitKey(1);
        }
        videoDevice.release();
    } else {
        System.out.println("Couldn't open video device");
    }


}

    private void skipFrames(Mat imageArray, VideoCapture videoDevice, int frames) {
        for (int i = 0; i < frames; i++) {
            videoDevice.read(imageArray);
        }
    }
}
