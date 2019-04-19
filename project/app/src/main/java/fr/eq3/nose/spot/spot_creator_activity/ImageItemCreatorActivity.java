package fr.eq3.nose.spot.spot_creator_activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.FileDescriptor;
import java.io.IOException;

import fr.eq3.nose.R;
import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.ImageItem;
import fr.eq3.nose.spot.spot_activity.SpotActivity;

public class ImageItemCreatorActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 36;

    private long spotId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.descriptible_creator_view);
        FloatingActionButton validate = findViewById(R.id.validateButton);
        validate.setOnClickListener(this::onClick);
        Intent intent = getIntent();
        this.spotId = intent.getLongExtra(SpotActivity.SPOT_EXTRA, -1);
    }


    private void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.setResult(resultCode);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                EditText name = findViewById(R.id.nameTextField);
                EditText desc = findViewById(R.id.descTextField);
                AddImageAsyncTask task = new AddImageAsyncTask(name.getText().toString(), desc.getText().toString(), this.spotId);
                task.execute(uri);
                Intent intent = new Intent();
                intent.putExtra(SpotActivity.SPOT_EXTRA, this.spotId);
                this.setResult(RESULT_OK, intent);
            }
        }
    }

    private final class AddImageAsyncTask extends AsyncTask<Uri, Void, Void> {

        private final String name, desc;
        private final long spotId;

        AddImageAsyncTask(String name, String desc, long spotId) {
            this.name = name;
            this.desc = desc;
            this.spotId = spotId;
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];
            try {
                Bitmap img = getBitmapFromUri(uri);
                new DatabaseRequest(ImageItemCreatorActivity.this).createImageItem(name, desc, img, spotId);
            } catch (IOException e) {
                Log.i("DIM", e.getMessage());
            }
            return null;
        }

        private Bitmap getBitmapFromUri(Uri uri) throws IOException {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ImageItemCreatorActivity.this.finish();
        }
    }
}
