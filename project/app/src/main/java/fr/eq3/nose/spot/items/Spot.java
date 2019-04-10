package fr.eq3.nose.spot.items;

import android.content.Context;

import java.util.ArrayList;

public interface Spot {
    String getName();
    ArrayList<ImageItem> getItems();
    void addItem(ImageItem imageItem);
    long getId();
    double getLat();
    double getLong();
    int getInfluenceLvl();
}
