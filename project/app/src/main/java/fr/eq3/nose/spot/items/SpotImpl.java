package fr.eq3.nose.spot.items;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

public final class SpotImpl implements Spot{

    private final String name;
    private final long id;
    private final ArrayList<ImageItem> items;
    private final ProgressiveImageLoader progressiveLoader;
    private final double lattitude, longitude;
    private final int influenceLvl;

    SpotImpl(long id, String name, double lat, double longitude, int influenceLvl, ProgressiveImageLoader progressiveLoader) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>();
        this.lattitude = lat;
        this.longitude = longitude;
        this.progressiveLoader = progressiveLoader;
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
    public void addItem(Context context, ImageItem imageItem) {
        this.items.add(imageItem);
        new DatabaseRequest(context).putImage(imageItem, this.id);
    }

    @Override
    public boolean loadMore(int totalItems, boolean isNeededToWait) {
        Collection<ImageItem> elements = progressiveLoader.getNextElements(totalItems, isNeededToWait);
        if(elements != null){
            this.items.addAll(elements);
            return true;
        }
        return false;
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
