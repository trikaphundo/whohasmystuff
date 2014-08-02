package de.freewarepoint.whohasmystuff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity {

    public static final String LOG_TAG = "WhoHasMyStuff";
    public static final String FIRST_START = "FirstStart";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (findViewById(R.id.mainActivity) != null) {
            if (savedInstanceState != null) {
                return;
            }

            ListLentObjects firstFragment = new ListLentObjects();

            firstFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainActivity, firstFragment).commit();
        }
    }

}
