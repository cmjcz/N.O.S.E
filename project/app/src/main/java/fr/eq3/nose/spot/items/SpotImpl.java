package fr.eq3.nose.spot.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public final class SpotImpl implements Spot{

    private String name;
    private String description;
    private final long id;
    private final ArrayList<ImageItem> items;
    private final double lattitude, longitude;
    private final int influenceLvl;

    SpotImpl(long id, String name, String description double lat, double longitude, int influenceLvl) {
        this.id = id;
        this.name = name;
        this.description = description;
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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDesription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeLong(this.id);
        dest.writeList(this.items);
        dest.writeDouble(this.lattitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.influenceLvl);

    }

    public static final Parcelable.Creator<SpotImpl> CREATOR
            = new Parcelable.Creator<SpotImpl>() {
        public SpotImpl createFromParcel(Parcel in) {
            return new SpotImpl(in);
        }

        public SpotImpl[] newArray(int size) {
            return new SpotImpl[size];
        }
    };

    private SpotImpl(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.id = in.readLong();
        this.items = in.readArrayList(ImageItem.class.getClassLoader());
        this.lattitude = in.readDouble();
        this.longitude = in.readDouble();
        this.influenceLvl = in.readInt();
    }
}
