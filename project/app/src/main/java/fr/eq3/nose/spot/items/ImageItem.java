package fr.eq3.nose.spot.items;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Element<Bitmap>{
    private Bitmap image;
    private String name;
    private String desc;
    private long id;

    public ImageItem(long id, Bitmap image, String title, String desc) {
        super();
        this.id = id;
        this.image = image;
        this.name = title;
        this.desc = desc;
    }

    public ImageItem(String title){
        super();
        this.id = -1;
        this.image = createEmptyBitmap(128, 128);
        this.name = title;
        this.desc = "";
    }

    public ImageItem(Parcel in) {
        this.name = in.readString();
        this.desc = in.readString();
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.id = in.readLong();
    }

    public Bitmap getData() {
        return image;
    }

    public void setData(Bitmap image) {
        this.image = image;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private static Bitmap createEmptyBitmap(int width, int heigh){
        int[] pixels = new int[width * heigh];
        for(int i = 0; i < width * heigh; ++i){
            pixels[i] = 0;
        }
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, heigh, Bitmap.Config.ARGB_4444);
        return bitmap;
    }

    @Override
    public String getDesription() {
        return desc;
    }

    @Override
    public void setDescription(String description) {
        this.desc = description;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeParcelable(this.image, flags);
        dest.writeLong(this.id);
    }

    public static final Parcelable.Creator<ImageItem> CREATOR
            = new Parcelable.Creator<ImageItem>() {
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}