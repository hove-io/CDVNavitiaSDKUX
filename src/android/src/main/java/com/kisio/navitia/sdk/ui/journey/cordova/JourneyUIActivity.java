package com.kisio.navitia.sdk.ui.journey.cordova;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.kisio.navitia.sdk.ui.journey.core.JourneyUI;
import com.kisio.navitia.sdk.ui.journey.core.JourneysRequest;
import com.kisio.navitia.sdk.ui.journey.presentation.ui.form.FormFragment;
import com.kisio.navitia.sdk.ui.journey.presentation.ui.journeys.JourneysFragment;

import kotlin.Pair;

public class JourneyUIActivity extends AppCompatActivity {

    public static final String DESTINATION = "arg:destination";
    public static final String JOURNEYS_REQUEST = "arg:JourneysRequest";
    public static final String ORIGIN = "arg:origin";
    public static final String WITH_FORM = "arg:withForm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_journey_ui);
        JourneyUI.Companion.getInstance().attachActivity(this);

        Intent intent = getIntent();
        if (intent != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment f;
            String tag;

            if (getIntent().getBooleanExtra(WITH_FORM, false)) {
                FormFragment formFragment = FormFragment.Companion.newInstance(
                    true,
                    (Pair<String, String>) intent.getSerializableExtra(ORIGIN),
                    (Pair<String, String>) intent.getSerializableExtra(DESTINATION)
                );
                f = formFragment;
                tag = formFragment.getSimpleTag();
            } else {
                JourneysFragment journeysFragment = JourneysFragment.Companion.newInstance(
                    (JourneysRequest) intent.getParcelableExtra(JOURNEYS_REQUEST),
                    true
                );
                f = journeysFragment;
                tag = journeysFragment.getSimpleTag();
            }

            ft.replace(R.id.activity_journey_ui_content, f, tag);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        JourneyUI.Companion.getInstance().getDelegate().onBackPressed();
    }
}
