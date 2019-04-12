package fr.eq3.nose.spot.items;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Element<Bitmap>{
    private Bitmap image;
    private String title;
    private String desc;

    public ImageItem(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
        this.desc = "";
    }

    public ImageItem(Parcel in) {
        this.title = in.readString();
        this.desc = in.readString();
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public Bitmap getData() {
        return image;
    }

    public void setData(Bitmap image) {
        this.image = image;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = title;
    }

    public static ImageItem createEmptyImageItem(int width, int heigh, String title){
        int[] pixels = new int[width * heigh];
        for(int i = 0; i < width * heigh; ++i){
            pixels[i] = 0;
        }
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, heigh, Bitmap.Config.ARGB_4444);
        return new ImageItem(bitmap, title);
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.desc);
        dest.writeParcelable(this.image, flags);
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