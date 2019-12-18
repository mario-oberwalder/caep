/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import java.util.List;

public interface ImageSourceDAOInterface {
    List<ImageSource> findIsidByMd5Hash(ImageSource imageSource);
    List<ImageSource> findAll(ImageSource imageSource);

    boolean insertImageSource(ImageSource imageSource);
    boolean updateImageSource(ImageSource imageSource);
    boolean deleteImageSource(ImageSource imageSource);
}
