package de.freewarepoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class AddObject extends Activity {

    private EditText mTypeText;
    private EditText mDescriptionText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add);
        setTitle(R.string.add_title);

        mTypeText = (EditText) findViewById(R.id.add_type);
        mDescriptionText = (EditText) findViewById(R.id.add_description);
        Button addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString(OpenLendDbAdapter.KEY_TYPE, mTypeText.getText().toString());
                bundle.putString(OpenLendDbAdapter.KEY_DESCRIPTION, mDescriptionText.getText().toString());

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();

            }
        });
    }
}