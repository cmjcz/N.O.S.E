package fr.eq3.nose.spot.spot_creator_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import fr.eq3.nose.R;

public class SpotCreatorActivity extends AppCompatActivity {
    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "desc";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_creator_view);
        FloatingActionButton validate = findViewById(R.id.validateButton);
        validate.setOnClickListener(v -> validate());
    }

    private void validate() {
        Intent intent = new Intent();
        EditText name = findViewById(R.id.nameTextField);
        intent.putExtra(KEY_NAME, name.getText().toString());
        EditText desc = findViewById(R.id.descTextField);
        intent.putExtra(KEY_DESC, desc.getText().toString());
        this.setResult(RESULT_OK, intent);
        finish();
    }
}
