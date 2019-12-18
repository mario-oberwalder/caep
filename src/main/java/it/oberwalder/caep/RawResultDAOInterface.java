/*
 * Copyright (c) 2019. Mario Oberwalder mario.oberwalder@gmail.com
 */

package it.oberwalder.caep;

import java.util.List;

public interface RawResultDAOInterface {
    List<ImageSource> findIsidByMd5Hash(ImageSource imageSource);

    List<ImageSource> findAllById(Long isid);


    boolean insertRawResult(RawResult rawResult);

    boolean updateImageSource(ImageSource imageSource);
    boolean deleteImageSource(ImageSource imageSource);
}
