package fr.eq3.nose.spot.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import java.io.FileDescriptor;
import java.io.IOException;

import fr.eq3.nose.R;
import fr.eq3.nose.spot.items.ImageItem;
import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.Spot;

public class SpotView extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 36;
    public static final String SPOT_EXTRA = "spot_extra";

    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    private Spot spot;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    Bitmap img = getBitmapFromUri(uri);
                    ImageItem imageItem = new ImageItem(img, "");
                    this.spot.addItem(this, imageItem);
                    gridViewAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    Log.i("DIM", e.getMessage());
                }
            }
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void onAddButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_view);
        FloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(this::onAddButtonClick);

        Intent intent = getIntent();
        int spotId = intent.getIntExtra(SPOT_EXTRA, -1);
        this.spot = new DatabaseRequest(this).getSpot(spotId);
        TextView tv = findViewById(R.id.spot_view_name);
        tv.setText(spot.getName());
        initializeGridView();

    }

    private void initializeGridView(){
        Log.i("DIM", "Initializing gridView");
        this.gridView = findViewById(R.id.spot_view_galery);
        this.gridViewAdapter = new GridViewAdapter(this, R.layout.spot_view_img, spot.getItems());
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnScrollListener(new EndlessScrollListener(6) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                return loadNextData(6, false);
            }
        });
        Log.i("DIM", "gridView initialized");
        loadNextData(8, true);
    }

    private boolean loadNextData(int totalItems, boolean isNeededToWait){
        Log.i("DIM", "Loading more data");
        boolean isMoreItemLoaded = this.spot.loadMore(totalItems, isNeededToWait);
        if(isMoreItemLoaded){
            gridViewAdapter.notifyDataSetChanged();
        }
        Log.i("DIM", isMoreItemLoaded ? "Data loaded !" : "Error when loading new data");
        return isMoreItemLoaded;
    }
}
