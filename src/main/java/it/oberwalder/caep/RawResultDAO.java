/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class RawResultDAO implements RawResultDAOInterface{
    Connection con;

    public RawResultDAO() {
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
        return null;
    }

    @Override
    public List<ImageSource> findAllById(Long isid) {
        return null;
    }

    @Override
    public boolean insertRawResult(RawResult rawResult) {
        Statement stmt;
        try {
            stmt = con.createStatement();
            String insertSql = "INSERT INTO rawresult("+
                    "currentname, currentpath,md5hash," +
                    "height,width,fps,framecount)"
                    + " VALUES('"+rawResult.getFilePath() +
                    "', '"+rawResult.getFilePath()+
                    "', '"+rawResult.getMd5Hash()+
                    "', "+rawResult.getHeight()+
                    ", "+rawResult.getWidth()+
                    ", "+rawResult.getFps()+
                    ", "+rawResult.getFrameCount()+")";
            stmt.executeUpdate(insertSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
