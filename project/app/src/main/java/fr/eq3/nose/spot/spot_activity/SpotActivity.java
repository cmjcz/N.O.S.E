package fr.eq3.nose.spot.spot_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Collection;

import fr.eq3.nose.R;
import fr.eq3.nose.spot.image_activity.ImageActivity;
import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.ImageItem;
import fr.eq3.nose.spot.items.Spot;
import fr.eq3.nose.spot.spot_creator_activity.ImageItemCreatorActivity;

public final class SpotActivity extends AppCompatActivity {

    private static final int CREATOR_REQUEST_CODE = 3587;
    public static final String SPOT_EXTRA = "spot_extra";

    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    private Spot spot;
    private ProgressiveImageLoader progressiveImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_view);
        FloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(this::onAddButtonClick);

        Intent intent = getIntent();
        long spotId = intent.getLongExtra(SPOT_EXTRA, -1);
        this.spot = new DatabaseRequest(this).getSpot(spotId);
        this.progressiveImageLoader = new ProgressiveImageLoader(this, spotId, new Runnable() {
            @Override
            public void run() {
                gridViewAdapter.notifyDataSetChanged();
            }
        });
        TextView desc = findViewById(R.id.spot_view_desc);
        desc.setText(spot.getDesription());
        initializeGridView();
        setTitle(spot.getName());
    }

    private void initializeGridView(){
        this.gridView = findViewById(R.id.spot_view_galery);
        this.gridViewAdapter = new GridViewAdapter(this, R.layout.spot_view_img, spot.getItems());
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnScrollListener(new EndlessScrollListener(6) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                return loadNextData(6);
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long image = spot.getItems().get(position).getId();

                Intent intent = new Intent(SpotActivity.this, ImageActivity.class);
                intent.putExtra(ImageActivity.IMAGE_EXTRA, image);
                startActivity(intent);
            }
        });
        loadNextData(8);
    }

    private boolean loadNextData(int totalItems){
        Collection<ImageItem> imageItems = this.progressiveImageLoader.getNextElements(totalItems);
        for(ImageItem img : imageItems){
            this.spot.addItem(img);
        }
        boolean isMoreItemLoaded = !imageItems.isEmpty();
        if(isMoreItemLoaded){
            gridViewAdapter.notifyDataSetChanged();
        }
        return isMoreItemLoaded;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATOR_REQUEST_CODE && resultCode == RESULT_OK) {
            gridViewAdapter.notifyDataSetChanged();
        }

    }

    private void onAddButtonClick(View view) {
        Intent intent = new Intent(this, ImageItemCreatorActivity.class);
        intent.putExtra(SPOT_EXTRA, this.spot.getId());
        startActivityForResult(intent, CREATOR_REQUEST_CODE);
    }


}
