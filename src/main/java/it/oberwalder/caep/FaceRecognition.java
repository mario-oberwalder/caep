/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.hconcat;
import static org.opencv.core.Core.vconcat;
import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgproc.Imgproc.rectangle;

public class FaceRecognition {
    private static final Integer NEURAL_NET_FRAME_HEIGHT = 300;
    private static final Integer NEURAL_NET_FRAME_WIDTH = 300;
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    private static final String NEURAL_NET_MODEL_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String NEURAL_NET_CONFIG_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\deploy.prototxt";
    private static final String TEST_MOVIE_PATH = "C:\\movies\\";
    private static final String TEST_MOVIE_FILENAME = "test2.mp4";
    Integer frameWidthFactor;
    Integer frameHeightFactor;
    VideoWriter videoOut;

    public FaceRecognition() {};

    public int startRecognition() {
    Mat imageArray = new Mat();

    VideoCapture videoDevice = new VideoCapture(TEST_MOVIE_PATH+TEST_MOVIE_FILENAME);
    videoDevice.open(TEST_MOVIE_PATH+TEST_MOVIE_FILENAME);
    Net net = readNetFromCaffe(NEURAL_NET_CONFIG_PATH, NEURAL_NET_MODEL_PATH);


        if (videoDevice.isOpened()) {
            int frameCounter = 0;
            namedWindow("Wonderful window", WINDOW_AUTOSIZE);
            skipFrames(videoDevice, 0);
            videoOut = new VideoWriter(LocalDateTime.now().getMinute()+".avi"
                    ,VideoWriter.fourcc('M','J','P','G')
                    ,videoDevice.get(5)
                    ,new Size(1920,1080));

            //while (readMultipleFramesUseOnlyLast(imageArray, videoDevice, 1)) {
            while (videoDevice.read(imageArray)) {
                List<Mat> subFrames;
                List<Mat> detectionMats;
                subFrames = splitFrameforNet(videoDevice, imageArray);
                detectionMats = sendImageThroughNet(subFrames, net);
                subFrames = computeResults(subFrames, detectionMats);
                Mat resultImage = buildResultImage(subFrames);
                showResults(resultImage);
                Mat videoImage = new Mat();
                Imgproc.resize(resultImage, videoImage,new Size(1920, 1080));
                videoOut.write(videoImage);
                frameCounter++;
                if (frameCounter > (int) videoDevice.get(7)){
                    break;
                }
            }
            videoDevice.release();
            videoOut.release();
            destroyAllWindows();
            return 0;
    } else {
        System.out.println("Couldn't open video device");
    }


        return 0;
    }

    private boolean readMultipleFramesUseOnlyLast(Mat imageArray, VideoCapture videoDevice, int counter) {
        for (int i = 0; i < counter-1; i++) {
            videoDevice.read(imageArray);
        }
        return videoDevice.read(imageArray);
    }

    private Mat buildResultImage(List<Mat> subFrames) {
        Mat outMat = new Mat(NEURAL_NET_FRAME_HEIGHT*frameHeightFactor, NEURAL_NET_FRAME_WIDTH*frameWidthFactor, subFrames.get(0).type());
        List<Mat> horizontalStrips = new ArrayList<>();
        for (int i = 0; i < frameHeightFactor; i++) {
            horizontalStrips.add(new Mat());
            hconcat(subFrames.subList(i*frameWidthFactor,(i+1)*frameWidthFactor-1), horizontalStrips.get(i));
        }
        vconcat(horizontalStrips,outMat);
    return outMat;
    }

    private List<Mat> splitFrameforNet(VideoCapture videoCapture, Mat imageArray) {
        frameWidthFactor = (int) Math.ceil(videoCapture.get(3)/NEURAL_NET_FRAME_WIDTH);
        frameHeightFactor = (int) Math.ceil(videoCapture.get(4)/NEURAL_NET_FRAME_HEIGHT);
        List<Mat> subFrames = new ArrayList<>();
        imageArray = expandFrameForDivision(imageArray,frameWidthFactor, frameHeightFactor);

            for (int j = 0; j < frameHeightFactor; j++) {
                for (int k = 0; k < frameWidthFactor; k++) {
                    Rect selectingRectangle = new Rect(k*NEURAL_NET_FRAME_WIDTH, j*NEURAL_NET_FRAME_HEIGHT
                            ,NEURAL_NET_FRAME_WIDTH,NEURAL_NET_FRAME_HEIGHT);
                    subFrames.add(new Mat(imageArray,selectingRectangle));
                }
            }
        return subFrames;
    }

    private Mat expandFrameForDivision(Mat imageArray, Integer frameWidthFactor, Integer frameHeightFactor) {
    Mat backGround = new Mat();
    backGround.create(
            new Size(frameWidthFactor*NEURAL_NET_FRAME_WIDTH
                    ,frameHeightFactor*NEURAL_NET_FRAME_HEIGHT),imageArray.type());
    imageArray.copyTo(backGround.submat(0,imageArray.rows(),0,imageArray.cols()));
    return backGround;
    }


    private List<Mat> computeResults(List<Mat> subFrames, List<Mat> detections) {
        for (int i = 0; i < subFrames.size(); i++) {
            Mat detectionMat = detections.get(i).reshape(1, (int) detections.get(i).total() / 7);
            for (int j = 0; j < detectionMat.rows(); j++) {
                double[] confidence = detectionMat.get(j, 2);
                if (confidence[0] > CONFIDENCE_THRESHOLD) {
                    int x1 = (int) ((detectionMat.get(j, 3)[0]) * 300);
                    int y1 = (int) ((detectionMat.get(j, 4)[0]) * 300);
                    int x2 = (int) ((detectionMat.get(j, 5)[0]) * 300);
                    int y2 = (int) ((detectionMat.get(j, 6)[0]) * 300);
                    rectangle(subFrames.get(i), new Point(x2, y2), new Point(x1, y1), new Scalar(0, 174, 255), 2, 4);
                }
            }
        }
        return subFrames;
    }

    private void showResults(Mat image) {
        Mat outImage;
        outImage = new Mat();
        Imgproc.resize(image, outImage,new Size(1280, 720));
        imshow("Wonderful window", outImage);
        waitKey(1);
    }

    private List<Mat> sendImageThroughNet(List<Mat> subFrames, Net net) {
        List<Mat> resultMats = new ArrayList<>();
        for (Mat subFrame:subFrames
             ) {
            Mat blob= blobFromImage(subFrame, 1.0, new Size(300, 300), new Scalar(104, 117, 123), false, false);
            net.setInput(blob);
            resultMats.add(net.forward().clone());
        }
        return resultMats;
    }

    private void skipFrames(VideoCapture videoDevice, int frames) {
        videoDevice.set(2,frames/videoDevice.get(7));
    }
}
