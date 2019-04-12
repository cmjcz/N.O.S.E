package fr.eq3.nose.spot.items;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;

    public ImageItem(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
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

}