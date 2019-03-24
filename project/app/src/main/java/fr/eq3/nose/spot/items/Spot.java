package fr.eq3.nose.spot.items;

import android.content.Context;

import java.util.ArrayList;

public interface Spot {
    String getName();
    ArrayList<ImageItem> getItems();
    void addItem(Context context, ImageItem imageItem);
    boolean loadMore(int totalItems, boolean isNeededToWait);
    long getId();
    double getLat();
    double getLong();
    int getInfluenceLvl();
}
