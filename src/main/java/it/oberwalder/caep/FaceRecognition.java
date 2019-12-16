/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgproc.Imgproc.rectangle;

public class FaceRecognition {
    private static final Integer NEURAL_NET_FRAME_HEIGHT = 300;
    private static final Integer NEURAL_NET_FRAME_WIDTH = 300;
    private static final double CONFIDENCE_THRESHOLD = 0.35;
    private static final String NEURAL_NET_MODEL_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String NEURAL_NET_CONFIG_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\deploy.prototxt";
    private static final String TEST_MOVIE_PATH = "C:\\movies\\test2.mp4";

    public FaceRecognition() {};

    public void startRecognition() {
    Mat imageArray = new Mat();
    Mat resizedImage = new Mat();
    Mat croppedImage = new Mat();
    Mat outImage = new Mat();

    VideoCapture videoDevice = new VideoCapture(TEST_MOVIE_PATH);
    videoDevice.open(TEST_MOVIE_PATH);
    Net net = readNetFromCaffe(NEURAL_NET_CONFIG_PATH, NEURAL_NET_MODEL_PATH);

        if (videoDevice.isOpened()) {
        namedWindow("Wonderful window", WINDOW_AUTOSIZE);

            skipFrames(videoDevice, 4800);
            while (videoDevice.read(imageArray)) {
                splitFrameforNet(videoDevice, imageArray);
                croppedImage = transfromImageForNet(imageArray, resizedImage);
                Mat detections = sendImageThroughNet(croppedImage, net);
                computeResults(croppedImage, detections);
                showResults(croppedImage);
            }
        videoDevice.release();
    } else {
        System.out.println("Couldn't open video device");
    }


}

    private void splitFrameforNet(VideoCapture videoCapture, Mat imageArray) {
        Integer frameWidthFactor = (int) Math.ceil(videoCapture.get(3)/NEURAL_NET_FRAME_WIDTH);
        Integer frameHeightFactor = (int) Math.ceil(videoCapture.get(4)/NEURAL_NET_FRAME_HEIGHT);

        imageArray = expandFrameForDivision(imageArray,frameWidthFactor, frameHeightFactor);

        List<Mat> subFrames = new ArrayList<>();
            for (int j = 0; j < frameHeightFactor; j++) {
                for (int k = 0; k < frameWidthFactor; k++) {
                    Rect selectingRectangle = new Rect(k*NEURAL_NET_FRAME_WIDTH, j*NEURAL_NET_FRAME_HEIGHT
                            ,NEURAL_NET_FRAME_WIDTH,NEURAL_NET_FRAME_HEIGHT);
                    subFrames.add(new Mat(imageArray,selectingRectangle));
                }
            }
        }

    private Mat expandFrameForDivision(Mat imageArray, Integer frameWidthFactor, Integer frameHeightFactor) {
    Mat backGround = new Mat();
    imageArray.copyTo(backGround);
    backGround.create(
            new Size(frameWidthFactor*NEURAL_NET_FRAME_WIDTH
                    ,frameHeightFactor*NEURAL_NET_FRAME_HEIGHT),imageArray.type());
    return backGround;
    }


    private void computeResults(Mat croppedImage, Mat detections) {
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
    }

    private void showResults(Mat croppedImage) {
        Mat outImage;
        outImage = new Mat();
        Imgproc.resize(croppedImage, outImage,new Size(900, 900));
        imshow("Wonderful window", outImage);
        waitKey(1);
    }

    private Mat sendImageThroughNet(Mat croppedImage, Net net) {
        Mat blob = blobFromImage(croppedImage, 1.0, new Size(300, 300), new Scalar(104, 117, 123), false, false);
        net.setInput(blob);
        return net.forward();
    }

    private Mat transfromImageForNet(Mat imageArray, Mat resizedImage) {
        Mat croppedImage;
        Imgproc.resize(imageArray, resizedImage, new Size(450, 300));
        Rect centerCut = new Rect(75, 0, 300, 300);
        croppedImage = new Mat(resizedImage, centerCut);
        return croppedImage;
    }

    private void skipFrames(VideoCapture videoDevice, int frames) {
        videoDevice.set(2,frames/videoDevice.get(7));
    }
}
