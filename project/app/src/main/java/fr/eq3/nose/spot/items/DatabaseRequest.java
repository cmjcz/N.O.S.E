package fr.eq3.nose.spot.items;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.eq3.nose.spot.items.exceptions.ImageNotFoundException;

public class DatabaseRequest extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "spots_db";
    private static final int DATABASE_VERSION = 76;

    private static final String TABLE_SPOTS = "spots";
    private static final String KEY_ID_SPOT = "id_spot";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "description";
    private static final String KEY_DATE = "date_of_creation";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";
    private static final String KEY_LVL = "influence_lvl";
    private static final String CREATE_TABLE_SPOTS = "CREATE TABLE " + TABLE_SPOTS + "(" +
            KEY_ID_SPOT + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT NOT NULL, " + KEY_DESC + " TEXT NOT NULL, " +
            KEY_DATE + " DATETIME NOT NULL, " + KEY_LAT + " REAL NOT NULL, " + KEY_LONG + " REAL NOT NULL, "+ KEY_LVL + " INTEGER NOT NULL" +");";

    private static final String TABLE_ITEMS = "items";
    private static final String KEY_COMMENTARY = "commentary";
    private static final String KEY_ID_ITEM = "id_item";
    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + "(" +
            KEY_ID_ITEM + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + KEY_COMMENTARY + " TEXT, " +
            KEY_DATE + " DATETIME, " + KEY_ID_SPOT + " INTEGER, " + " FOREIGN KEY(" +KEY_ID_SPOT +
            ") REFERENCES " + TABLE_SPOTS +  "(" + KEY_ID_SPOT + ")" +
            ");";

    private static final String TABLE_IMAGES = "images";
    private static final String CREATE_TABLE_IMAGES = "CREATE TABLE " + TABLE_IMAGES + "(" +
            KEY_ID_ITEM + " INTEGER PRIMARY KEY, "+  " FOREIGN KEY("
            + KEY_ID_ITEM + ") REFERENCES " + TABLE_ITEMS +
            ");";
    private static final String IMAGE_EXT = ".bimg";

    private Context context;

    public DatabaseRequest(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public Spot getSpot(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SPOTS
                + " WHERE " + KEY_ID_SPOT + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c == null ){
            return null;
        }
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(KEY_NAME));
        String desc = c.getString(c.getColumnIndex(KEY_DESC));
        double lat = c.getDouble(c.getColumnIndex(KEY_LAT));
        double longitude = c.getDouble(c.getColumnIndex(KEY_LONG));
        int lvl = c.getInt(c.getColumnIndex(KEY_LVL));
        c.close();
        db.close();
        return new SpotImpl(id, name, desc, lat, longitude, lvl);

    }

    public List<Integer> getSpotsBetween(double latitude, double longitude, double radius){
        double latMin = latitude - radius;
        double latMax = latitude + radius;
        double longMin = longitude - radius;
        double longMax = longitude + radius;
        ArrayList<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT " + KEY_ID_SPOT +" FROM " + TABLE_SPOTS
                + " WHERE " + KEY_LAT +  ">=" + latMin + " AND " + KEY_LAT + " <= " + latMax
                + " AND " + KEY_LONG +  ">=" + longMin + " AND " + KEY_LONG + " <= " + longMax;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c == null ){
            return ids;
        }
        if(c.moveToFirst()){
            do{
                int id = c.getInt(c.getColumnIndex(KEY_ID_SPOT));
                ids.add(id);
            }while (c.moveToNext());
        }
        c.close();
        db.close();
        return ids;
    }

    public List<Integer> getItemsIdsOfSpot(long spotId) {

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
        db.close();
        return ids;
    }

    public ImageItem getImage(int id) throws ImageNotFoundException {

        if(id == -1){
            return new ImageItem("null");
        }else{
            try {
                SQLiteDatabase db = getReadableDatabase();
                String selectQuery = "SELECT " + KEY_NAME + ", " + KEY_COMMENTARY + " FROM " + TABLE_ITEMS +
                        " WHERE " + TABLE_ITEMS + "."+  KEY_ID_ITEM + " = " + id;
                Cursor c = db.rawQuery(selectQuery, null);
                if(c == null ){
                    throw new ImageNotFoundException("Image not found on database");
                }
                if(c.moveToFirst()){
                    String name = c.getString(c.getColumnIndex(KEY_NAME));
                    String desc = c.getString(c.getColumnIndex(KEY_COMMENTARY));
                    return getImageFromFile(id, name, desc);
                }
                throw new ImageNotFoundException("Image not found on database");
            } catch (IOException e) {
                throw new ImageNotFoundException("Image not found on system storage");
            }

        }
    }

    private ImageItem getImageFromFile(int id, String name, String desc) throws IOException {
        File path = this.context.getFilesDir();
        File img = new File(path, id + IMAGE_EXT);
        FileInputStream in;
        in = new FileInputStream(img);
        byte[] bytes = new byte[(int) img.length()];
        in.read(bytes);
        in.close();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return new ImageItem(bitmap, name);
    }

    public Spot createSpot(String name, String description, LatLng position){
        SQLiteDatabase db = getWritableDatabase();

        final int START_LVL = 1;

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_DESC, description);
        values.put(KEY_DATE, getdate());
        values.put(KEY_LAT, position.latitude);
        values.put(KEY_LONG, position.longitude);
        values.put(KEY_LVL, START_LVL);

        long id = db.insert(TABLE_SPOTS, null, values);
        return new SpotImpl(id, name, position.latitude, position.longitude, START_LVL);
    }

    private String getdate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public long putImage(ImageItem imageItem, long spotId) {
        SQLiteDatabase db = getWritableDatabase();
        return putImage(db, imageItem, spotId);
    }

    private long putImage(SQLiteDatabase db, ImageItem imageItem, long spotId){
        long itemId = putItem(db, imageItem.getName(), "", spotId);
        ContentValues image_values = new ContentValues();
        putImageInFile(imageItem, itemId);
        image_values.put(KEY_ID_ITEM, itemId);
        db.insert(TABLE_IMAGES, null, image_values);
        db.close();
        return itemId;
    }

    private void putImageInFile(ImageItem imageItem, long itemId){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        imageItem.getData().compress(Bitmap.CompressFormat.PNG, 0, os);
        byte[] imageBytes = os.toByteArray();
        File path = this.context.getFilesDir();
        File img = new File(path, itemId + IMAGE_EXT);
        try {
            FileOutputStream stream = new FileOutputStream(img);
            stream.write(imageBytes);
            stream.close();
        } catch (IOException e) {
            Log.i("DIM", "Error when saving the file");
        }
    }

    private long putItem(SQLiteDatabase db, String name, String commentary, long spotId){
        ContentValues items_values = new ContentValues();
        items_values.put(KEY_NAME, name);
        items_values.put(KEY_COMMENTARY, commentary);
        items_values.put(KEY_DATE, getdate());
        items_values.put(KEY_ID_SPOT, spotId);
        long id = db.insert(TABLE_ITEMS, null, items_values);
        return id;
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
