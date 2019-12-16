/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import org.junit.Assert;
import org.junit.Test;

public class FaceRecTest {

    @Test
    public void testFaceRecognitionConstructor() {
        FaceRecognition faceRecognition = new FaceRecognition();
        Assert.assertTrue(faceRecognition instanceof FaceRecognition);

    }
}