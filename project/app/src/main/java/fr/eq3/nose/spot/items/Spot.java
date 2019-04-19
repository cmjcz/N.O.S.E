package fr.eq3.nose.spot.items;

import android.content.Context;

import java.util.ArrayList;

public interface Spot extends Descriptible{
    String getName();
    ArrayList<ImageItem> getItems();
    void addItem(ImageItem imageItem);
    double getLat();
    double getLong();
    int getInfluenceLvl();
}
