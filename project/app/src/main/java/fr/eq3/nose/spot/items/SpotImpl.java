package fr.eq3.nose.spot.items;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import fr.eq3.nose.spot.view.ProgressiveImageLoader;

public final class SpotImpl implements Spot{

    private final String name;
    private final long id;
    private final ArrayList<ImageItem> items;
    private final double lattitude, longitude;
    private final int influenceLvl;

    SpotImpl(long id, String name, double lat, double longitude, int influenceLvl) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>();
        this.lattitude = lat;
        this.longitude = longitude;
        this.influenceLvl = influenceLvl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<ImageItem> getItems() {
        return items;
    }

    @Override
    public void addItem(ImageItem imageItem) {
        this.items.add(imageItem);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public double getLat() {
        return this.lattitude;
    }

    @Override
    public double getLong() {
        return this.longitude;
    }

    @Override
    public int getInfluenceLvl() {
        return this.influenceLvl;
    }
}
