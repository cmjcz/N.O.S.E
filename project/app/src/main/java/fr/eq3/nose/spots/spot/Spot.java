package fr.eq3.nose.spots.spot;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;

import fr.eq3.nose.spots.Item.ImageItem;

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
