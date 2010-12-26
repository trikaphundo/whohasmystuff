package de.freewarepoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

public class AddObject extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=new Intent();

        Bundle bundle = new Bundle();
        bundle.putString("name", "New Object");
        bundle.putString("type", "New Type");
        intent.putExtra("return", bundle);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
}