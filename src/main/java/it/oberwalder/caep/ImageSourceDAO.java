/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageSourceDAO implements  ImageSourceDAOInterface{
    Connection con;

    public ImageSourceDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Couldn't load JDBC-Driver");
        }
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/caep", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Couldn't get Database connection");
        }
    }

    @Override
    public List<ImageSource> findIsidByMd5Hash(ImageSource imageSource) {
        List<ImageSource> returnList = new ArrayList<>();
        try {
            Statement stmt = con.createStatement();
            String selectImageSource = "SELECT * From imagesources WHERE md5hash = "+imageSource.getMd5Hash();
            ResultSet resultSet = stmt.executeQuery(selectImageSource);
            imageSource.setIsid(resultSet.getLong("isid"));
            returnList.add(imageSource);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL statement creation failed");
        }
        return returnList;
    }

    @Override
    public List<ImageSource> findAll(ImageSource imageSource) {
        return null;
    }

    @Override
    public boolean insertImageSource(ImageSource imageSource) {
        Statement stmt;
        try {
            stmt = con.createStatement();
            String insertSql = "INSERT INTO imagesource("+
                    "currentname, currentpath,md5hash," +
                    "height,width,fps,framecount)"
                    + " VALUES('"+imageSource.getFilePath() +
                    "', '"+imageSource.getFilePath()+
                    "', '"+imageSource.getMd5Hash()+
                    "', "+imageSource.getHeight()+
                    ", "+imageSource.getWidth()+
                    ", "+imageSource.getFps()+
                    ", "+imageSource.getFrameCount()+")";
            stmt.executeUpdate(insertSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateImageSource(ImageSource imageSource) {
        return false;
    }

    @Override
    public boolean deleteImageSource(ImageSource imageSource) {
        return false;
    }
}
