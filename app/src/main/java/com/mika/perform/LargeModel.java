package com.mika.perform;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: mika
 * @Time: 2019-11-12 11:27
 * @Description:
 */
public class LargeModel implements Serializable {

    private String name;
    private List<Bitmap> bitmaps = new ArrayList<>(100);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Bitmap> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(List<Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }
}
