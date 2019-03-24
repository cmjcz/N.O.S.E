package fr.eq3.nose.spots.spot;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.Collection;

import fr.eq3.nose.spots.Item.ImageItem;
import fr.eq3.nose.spots.loader.ProgressiveLoader;
import fr.eq3.nose.spots.request_to_db.DatabaseRequest;

public final class SpotImpl implements Spot{

    private final String name;
    private final long id;
    private final ArrayList<ImageItem> items;
    private final ProgressiveLoader<ImageItem> progressiveLoader;
    private final double lattitude, longitude;
    private final int influenceLvl;

    public SpotImpl(long id, String name, double lat, double longitude, int influenceLvl, ProgressiveLoader<ImageItem> progressiveLoader) {
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
