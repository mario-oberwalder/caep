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
import static org.opencv.imgproc.Imgproc.*;

public class FaceRecognition {
    static final Integer NEURAL_NET_FRAME_HEIGHT = 300;
    static final Integer NEURAL_NET_FRAME_WIDTH = 300;
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    private static final double CONFIDENCE_THRESHOLD_SAVE = 0.2;
    private static final String NEURAL_NET_MODEL_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\res10_300x300_ssd_iter_140000_fp16.caffemodel";
    private static final String NEURAL_NET_CONFIG_PATH = "C:\\opencv\\sources\\samples\\dnn\\face_detector\\deploy.prototxt";
    private static final String WINDOW_NAME = "wonderful window";
    static Double frameTime = 0d;
    ImageSource imageSource = new ImageSource();
    VideoWriter videoOut;
    List<RawResult> resultsData = new ArrayList<>();

    public FaceRecognition() {
    }

    public void startRecognition() {
        Mat imageArray = new Mat();
        VideoCapture videoDevice = new VideoCapture();
        videoDevice.open(imageSource.getFilePath());
        ImageSourceDAO imageSourceDAO = new ImageSourceDAO();
        imageSourceDAO.insertImageSource(imageSource);
        Net net = readNetFromCaffe(NEURAL_NET_CONFIG_PATH, NEURAL_NET_MODEL_PATH);

        if (videoDevice.isOpened()) {
            namedWindow(WINDOW_NAME, WINDOW_AUTOSIZE);
            videoOut = provideVideoOut();
            while (videoDevice.read(imageArray)) {
                frameTime++;
                List<Mat> ROIsFromImage;
                List<Mat> detectionMats;
                imageArray = expandFrameForDivision(imageArray,0,0);
                ROIsFromImage = splitFrameForNet(imageArray);
                detectionMats = sendFramesThroughNet(ROIsFromImage, net);
                ROIsFromImage = applyEffects(ROIsFromImage, detectionMats);
                Mat outputImage= imageArray.submat(0,(int) imageSource.getHeight(),0,(int) imageSource.getWidth());
                showResults(outputImage);
                videoOut.write(outputImage);

                if (frameTime == imageSource.getFrameCount()-10) {
                    break;
                }
            }
            videoDevice.release();
            videoOut.release();
            destroyAllWindows();
        } else {
            System.out.println("Couldn't open video device");
        }
    }

    private VideoWriter provideVideoOut() {
        return new VideoWriter(LocalDateTime.now().getHour() +""+ LocalDateTime.now().getMinute() + ".avi"
                , VideoWriter.fourcc('M', 'J', 'P', 'G')
                , imageSource.getFps()
                , new Size(imageSource.getWidth(), imageSource.getHeight()));
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
        for (int j = 0; j < imageSource.getVerticalTiles()*2-1; j++) {
            for (int k = 0; k < imageSource.getHorizontalTiles()*2-1; k++) {
                Rect selectingRectangle = new Rect(k * NEURAL_NET_FRAME_WIDTH/2, j * NEURAL_NET_FRAME_HEIGHT/2
                        , NEURAL_NET_FRAME_WIDTH, NEURAL_NET_FRAME_HEIGHT);
                Mat subFrame = imageArray.submat(selectingRectangle);
                subFrames.add(subFrame);
            }
        }
        return subFrames;
    }

    public Integer getHorizontalTiles(Mat Image,Integer addedSpace) {
        Double returnValue;
        returnValue = Math.ceil((Image.cols()+addedSpace)/(double)FaceRecognition.NEURAL_NET_FRAME_WIDTH);
        return returnValue.intValue();
    }

    public Integer getVerticalTiles(Mat Image,Integer addedSpace) {
        Double returnValue;
        returnValue = Math.ceil((Image.rows()+addedSpace) /(double) FaceRecognition.NEURAL_NET_FRAME_HEIGHT);
        return returnValue.intValue();
    }

    private Mat expandFrameForDivision(Mat imageArray, int rowStart, int colStart) {
        Mat expandedFrame = new Mat();
        expandedFrame.create(
                new Size(getHorizontalTiles(imageArray,colStart) * NEURAL_NET_FRAME_WIDTH
                        , getVerticalTiles(imageArray,rowStart) * NEURAL_NET_FRAME_HEIGHT)
                        , imageArray.type());
        imageArray.copyTo(expandedFrame.submat(
                rowStart, imageArray.rows()+rowStart
                , colStart, imageArray.cols()+colStart));
        return expandedFrame;
    }


    private List<Mat> applyEffects(List<Mat> subFrames, List<Mat> detections) {
        RawResultDAO rawResultDAO = new RawResultDAO();
        for (int i = 0; i < subFrames.size(); i++) {
            Mat detectionMat = detections.get(i).reshape(1, (int) detections.get(i).total() / 7);
            for (int j = 0; j < detectionMat.rows(); j++) {
                double confidence = detectionMat.get(j, 2)[0];
                if (confidence > CONFIDENCE_THRESHOLD_SAVE) {
                    Point point1 = pointFromDetection(detectionMat, j, 3, 4);
                    Point point2 = pointFromDetection(detectionMat, j, 5, 6);
                    resultsData.add(new RawResult(frameTime, confidence, point1, point2));
                    rawResultDAO.insertRawResult(new RawResult(frameTime,frameTime,confidence,point1,point2));
                    if (confidence > CONFIDENCE_THRESHOLD) {
                        Rect faceFrame = new Rect(point1, point2);
                        GaussianBlur(subFrames.get(i).submat(faceFrame)
                                , subFrames.get(i).submat(faceFrame)
                                ,new Size(45,45)
                                ,0);
                        //rectangle(subFrames.get(i), point2, point1, new Scalar(0, 174, 255), 2, 4);
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
