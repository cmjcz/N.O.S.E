package fr.eq3.nose.spot.image_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import fr.eq3.nose.R;
import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.Element;
import fr.eq3.nose.spot.items.ImageItem;
import fr.eq3.nose.spot.items.exceptions.ImageNotFoundException;

public class ImageActivity extends AppCompatActivity {

    public static final String IMAGE_EXTRA = "image_extra";

    private ImageItem image;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);

        Intent intent = getIntent();
        long imgId = intent.getLongExtra( IMAGE_EXTRA, -1);
        try {
            this.image = new DatabaseRequest(this).getImage((int) imgId);
            TextView desc = findViewById(R.id.textView_imageDesc);
            ImageView imageView = findViewById(R.id.imageView_image);
            desc.setText(image.getDesription());
            imageView.setImageBitmap(image.getData());
            setTitle(image.getName());
        } catch (ImageNotFoundException e) {
            e.printStackTrace();
        }



    }
}
