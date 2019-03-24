package fr.eq3.nose.spot.items;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseRequest extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "spots_db";
    private static final int DATABASE_VERSION = 13;

    private static final double TMP_LAT = 37.4220462, TMP_LONG = -122.084028;

    private static final String TABLE_SPOTS = "spots";
    private static final String KEY_ID_SPOT = "id_spot";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "description";
    private static final String KEY_DATE = "date_of_creation";
    private static final String CREATE_TABLE_SPOTS = "CREATE TABLE " + TABLE_SPOTS + "(" +
            KEY_ID_SPOT + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT NOT NULL, " + KEY_DESC + " TEXT NOT NULL, " +
            KEY_DATE + " DATETIME NOT NULL" + ");";

    private static final String TABLE_ITEMS = "items";
    private static final String KEY_COMMENTARY = "commentary";
    private static final String KEY_ID_ITEM = "id_item";
    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + "(" +
            KEY_ID_ITEM + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + KEY_COMMENTARY + " TEXT, " +
            KEY_DATE + " DATETIME, " + KEY_ID_SPOT + " INTEGER, " + " FOREIGN KEY(" +KEY_ID_SPOT +
            ") REFERENCES " + TABLE_SPOTS +  "(" + KEY_ID_SPOT + ")" +
            ");";

    private static final String TABLE_IMAGES = "images";
    private static final String KEY_IMAGE_DATA = "data";
    private static final String CREATE_TABLE_IMAGES = "CREATE TABLE " + TABLE_IMAGES + "(" +
            KEY_ID_ITEM + " INTEGER PRIMARY KEY, " + KEY_IMAGE_DATA + " BLOB, "+  " FOREIGN KEY("
            + KEY_ID_ITEM + ") REFERENCES " + TABLE_ITEMS +
            ");";

    private Context context;

    public DatabaseRequest(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public Spot getSpot(int id) {
       ProgressiveImageLoader progressiveLoader = new ProgressiveImageLoader(
                id, context);
        return new SpotImpl(id, "Spot #" + id, TMP_LAT, TMP_LONG, 1, progressiveLoader);

    }

    List<Integer> getItemsIdsOfSpot(long spotId) {

        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT " + KEY_ID_ITEM + " FROM " + TABLE_ITEMS +
        " WHERE " + TABLE_ITEMS + "."+  KEY_ID_SPOT + " = " + spotId;
        Cursor c = db.rawQuery(selectQuery, null);
        if(c == null ){
            return null;
        }
        ArrayList<Integer> ids = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                int index = c.getColumnIndex(KEY_ID_ITEM);
                int id = c.getInt(index);
                ids.add(id);
            }while(c.moveToNext());
        }
        c.close();
        return ids;
    }

    ImageItem getImage(int id) {

        if(id == -1){
            return ImageItem.createEmptyImageItem(1, 1, "null");
        }

        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT " + KEY_IMAGE_DATA + " FROM " + TABLE_IMAGES
                + " WHERE " + KEY_ID_ITEM + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c == null ){
            return null;
        }
        c.moveToFirst();
        byte[] imageBytes = c.getBlob(c.getColumnIndex(KEY_IMAGE_DATA));
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        c.close();
        return new ImageItem(bitmap, "Image #"+id);
    }

    public Spot createSpot(String name, String description, LatLng position){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_DESC, description);
        values.put(KEY_DATE, getdate());

        long id = db.insert(TABLE_SPOTS, null, values);
        return new SpotImpl(id, name, position.latitude, position.longitude, 1,
                new ProgressiveImageLoader(id, this.context));
    }

    private String getdate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    long putImage(ImageItem imageItem, long spotId) {
        SQLiteDatabase db = getWritableDatabase();
        return putImage(db, imageItem, spotId);
    }

    private long putImage(SQLiteDatabase db, ImageItem imageItem, long spotId){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        imageItem.getImage().compress(Bitmap.CompressFormat.PNG, 0, os);
        byte[] imageBytes = os.toByteArray();
        long itemId = putItem(db, imageItem.getTitle(), "", spotId);
        ContentValues image_values = new ContentValues();
        image_values.put(KEY_IMAGE_DATA, imageBytes);
        image_values.put(KEY_ID_ITEM, itemId);
        return db.insert(TABLE_IMAGES, null, image_values);
    }

    private long putItem(SQLiteDatabase db, String name, String commentary, long spotId){
        ContentValues items_values = new ContentValues();
        items_values.put(KEY_NAME, name);
        items_values.put(KEY_COMMENTARY, commentary);
        items_values.put(KEY_DATE, getdate());
        items_values.put(KEY_ID_SPOT, spotId);
        return db.insert(TABLE_ITEMS, null, items_values);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SPOTS);
        db.execSQL(CREATE_TABLE_ITEMS);
        db.execSQL(CREATE_TABLE_IMAGES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP = "DROP TABLE IF EXISTS ";
        db.execSQL(DROP + TABLE_IMAGES);
        db.execSQL(DROP + TABLE_ITEMS);
        db.execSQL(DROP + TABLE_SPOTS);
        onCreate(db);
    }
}
