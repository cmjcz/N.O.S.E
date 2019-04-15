package fr.eq3.nose.spot.spot_creator_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import fr.eq3.nose.R;
import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.Spot;

public class SpotCreatorActivity extends AppCompatActivity {
    public static final String KEY_SPOT = "spot";
    public static final String KEY_POS = "pos";
    private LatLng mylocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.descriptible_creator_view);
        FloatingActionButton validate = findViewById(R.id.validateButton);
        validate.setOnClickListener(this::onClick);
        mylocation = getIntent().getParcelableExtra(KEY_POS);
    }

    private void validate() {
        EditText name = findViewById(R.id.nameTextField);
        EditText desc = findViewById(R.id.descTextField);
        DatabaseRequest dbr = new DatabaseRequest(this);
        Spot spot = dbr.createSpot(name.getText().toString(),
                desc.getText().toString(),
                mylocation);
        Intent intent = new Intent();
        intent.putExtra(KEY_SPOT, spot);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void onClick(View v) {
        validate();
    }
}
