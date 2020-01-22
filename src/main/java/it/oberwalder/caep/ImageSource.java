/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageSource {

    private String filePath;
    private String fileName;
    private String fileLocation;
    private String md5Hash;
    private Long isid;
    private double width;
    private double height;
    private double fps;
    private double frameCount;
    private Integer horizontalTiles;
    private Integer verticalTiles;

    public ImageSource() {
        this.fileLocation = "C:\\movies\\";
        this.fileName = "test2.mp4";
        this.filePath = fileLocation + fileName;
        calculatePropertiesFromFile();
    }

    private void calculatePropertiesFromFile() {
        VideoCapture videoDevice = new VideoCapture();
        videoDevice.open(this.getFilePath());

        if(!videoDevice.isOpened()){
            System.out.println("Couldn't open image source");
            }
        this.width = videoDevice.get(3);
        this.height = videoDevice.get(4);
        this.fps = videoDevice.get(5);
        this.frameCount = videoDevice.get(7);
        this.horizontalTiles = (int) Math.ceil(width/(double)FaceRecognition.NEURAL_NET_FRAME_WIDTH);
        this.verticalTiles = (int) Math.ceil(height/(double)FaceRecognition.NEURAL_NET_FRAME_HEIGHT);
        try {
            this.md5Hash = calculateMd5Hash();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoDevice.release();
    }

    private String calculateMd5Hash() throws NoSuchAlgorithmException, IOException {
        String filename = this.getFilePath();
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(Paths.get(filename)));
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getFps() {
        return fps;
    }

    public double getFrameCount() {
        return frameCount;
    }

    public Integer getHorizontalTiles() {
        return horizontalTiles;
    }

    public Integer getHorizontalTiles(Double overlapHorizontal) {
        double width;
        int tileNumber;
        width = this.width - FaceRecognition.NEURAL_NET_FRAME_WIDTH;
        tileNumber = (int) (width/(FaceRecognition.NEURAL_NET_FRAME_WIDTH*(1-overlapHorizontal)));
        return tileNumber+1;
    }

    public Integer getVerticalTiles() {
        return verticalTiles;
    }
    public Integer getVerticalTiles(Double overlapVertical) {
        double height;
        int tileNumber;
        height = this.height - FaceRecognition.NEURAL_NET_FRAME_HEIGHT;
        tileNumber = (int) (height/(FaceRecognition.NEURAL_NET_FRAME_HEIGHT*(1-overlapVertical)));
        return tileNumber+1;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMd5Hash() {
        return  md5Hash;
    }

    public void setIsid(long isid) {
        this.isid = isid;
    }
}
