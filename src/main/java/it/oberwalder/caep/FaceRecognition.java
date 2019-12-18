/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.core.*;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.hconcat;
import static org.opencv.core.Core.vconcat;
import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.rectangle;

public class FaceRecognition {
    static final Integer NEURAL_NET_FRAME_HEIGHT = 300;
    static final Integer NEURAL_NET_FRAME_WIDTH = 300;
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    private static final String NEURAL_NET_MODEL_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String NEURAL_NET_CONFIG_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\deploy.prototxt";
    private static final String WINDOW_NAME = "wonderful window";
    static Double timeStamp = 0d;
    ImageSource imageSource = new ImageSource();
    VideoWriter videoOut;
    List<RawResult> resultsData = new ArrayList<>();

    public FaceRecognition() {
    }

    public void startRecognition() {
        Mat imageArray = new Mat();
        VideoCapture videoDevice = new VideoCapture();
        videoDevice.open(imageSource.getFilePath());
        Net net = readNetFromCaffe(NEURAL_NET_CONFIG_PATH, NEURAL_NET_MODEL_PATH);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/caep", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }



        if (videoDevice.isOpened()) {
            namedWindow(WINDOW_NAME, WINDOW_AUTOSIZE);

            videoOut = new VideoWriter(LocalDateTime.now().getHour() +""+ LocalDateTime.now().getMinute() + ".avi"
                    , VideoWriter.fourcc('M', 'J', 'P', 'G')
                    , imageSource.getFps()
                    , new Size(imageSource.getWidth(), imageSource.getHeight()));

            while (videoDevice.read(imageArray)) {
                timeStamp++;
                List<Mat> subFrames;
                List<Mat> detectionMats;

                subFrames = splitFrameForNet(imageArray);
                detectionMats = sendFramesThroughNet(subFrames, net);
                subFrames = applyEffects(subFrames, detectionMats);
                Mat resultImage = buildResultImageFromFrames(subFrames);
                Mat outImage = new Mat(resultImage, new Rect(0, 0
                        , (int) imageSource.getWidth()
                        , (int) imageSource.getHeight()));
                showResults(outImage);
                videoOut.write(outImage);
            }
            videoDevice.release();
            videoOut.release();
            destroyAllWindows();
        } else {
            System.out.println("Couldn't open video device");
        }
    }

    private Mat buildResultImageFromFrames(List<Mat> subFrames) {
        List<Mat> horizontalStrips = new ArrayList<>();
        Mat outMat = new Mat(
                NEURAL_NET_FRAME_HEIGHT * imageSource.getVerticalTiles()
                , NEURAL_NET_FRAME_WIDTH * imageSource.getHorizontalTiles()
                , subFrames.get(0).type());

        for (int i = 0; i < imageSource.getVerticalTiles(); i++) {
            horizontalStrips.add(new Mat());
            hconcat(
                    subFrames.subList(i * imageSource.getHorizontalTiles(), ((i + 1) * imageSource.getHorizontalTiles()))
                    , horizontalStrips.get(i));
        }
        vconcat(horizontalStrips, outMat);
        return outMat;
    }

    private List<Mat> splitFrameForNet( Mat imageArray) {
        List<Mat> subFrames = new ArrayList<>();
        imageArray = expandFrameForDivision(imageArray, imageSource.getHorizontalTiles(), imageSource.getVerticalTiles());

        for (int j = 0; j < imageSource.getVerticalTiles(); j++) {
            for (int k = 0; k < imageSource.getHorizontalTiles(); k++) {
                Rect selectingRectangle = new Rect(k * NEURAL_NET_FRAME_WIDTH, j * NEURAL_NET_FRAME_HEIGHT
                        , NEURAL_NET_FRAME_WIDTH, NEURAL_NET_FRAME_HEIGHT);
                Mat subFrame = new Mat(imageArray, selectingRectangle);
                rectangle(subFrame, new Point(0,0), new Point(300,300), new Scalar(100,100,100), 2, 4);
                subFrames.add(subFrame);
            }
        }
        return subFrames;
    }

    private Mat expandFrameForDivision(Mat imageArray, Integer frameWidthFactor, Integer frameHeightFactor) {
        Mat backGround = new Mat();
        backGround.create(
                new Size(frameWidthFactor * NEURAL_NET_FRAME_WIDTH
                        , frameHeightFactor * NEURAL_NET_FRAME_HEIGHT)
                        , imageArray.type());
        imageArray.copyTo(backGround.submat(
                0, imageArray.rows()
                , 0, imageArray.cols()));
        return backGround;
    }


    private List<Mat> applyEffects(List<Mat> subFrames, List<Mat> detections) {
        for (int i = 0; i < subFrames.size(); i++) {
            Mat detectionMat = detections.get(i).reshape(1, (int) detections.get(i).total() / 7);
            for (int j = 0; j < detectionMat.rows(); j++) {
                double confidence = detectionMat.get(j, 2)[0];
                if (confidence > 0.1d) {
                    Point point1 = pointFromDetection(detectionMat, j, 3, 4);
                    Point point2 = pointFromDetection(detectionMat, j, 5, 6);
                    resultsData.add(new RawResult(timeStamp, confidence, point1, point2));
                    if (confidence > CONFIDENCE_THRESHOLD) {
                        Rect faceFrame = new Rect(point1, point2);
                        GaussianBlur(subFrames.get(i).submat(faceFrame)
                                , subFrames.get(i).submat(faceFrame)
                                ,new Size(45,45)
                                ,0);
                        rectangle(subFrames.get(i), point2, point1, new Scalar(0, 174, 255), 2, 4);
                    }
                }
            }
        }
        return subFrames;
    }

    private Point pointFromDetection(Mat detectionMat, int j, int i, int i1) {
        return new Point(
                (detectionMat.get(j, i)[0]) * NEURAL_NET_FRAME_WIDTH
                ,(detectionMat.get(j, i1)[0]) * NEURAL_NET_FRAME_HEIGHT);
    }

    private void showResults(Mat image) {
        Mat outImage;
        outImage = new Mat();
        Imgproc.resize(image, outImage, new Size(1280, 720));
        imshow(WINDOW_NAME, outImage);
        waitKey(1);
    }

    private List<Mat> sendFramesThroughNet(List<Mat> subFrames, Net net) {
        List<Mat> resultMats = new ArrayList<>();
        for (Mat subFrame : subFrames
        ) {
            Mat blob = blobFromImage(subFrame, 1.0, new Size(300, 300), new Scalar(104, 117, 123), false, false);
            net.setInput(blob);
            resultMats.add(net.forward().clone());
        }
        return resultMats;
    }

    private void skipApproxNumberOfFrames(VideoCapture videoDevice, int frames) {
        videoDevice.set(2, frames / videoDevice.get(7));
    }
}
