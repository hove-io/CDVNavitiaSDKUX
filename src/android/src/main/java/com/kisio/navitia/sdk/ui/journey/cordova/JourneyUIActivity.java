package com.kisio.navitia.sdk.ui.journey.cordova;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.kisio.navitia.sdk.ui.journey.core.ExpertEnvironment;
import com.kisio.navitia.sdk.ui.journey.core.JourneyColors;
import com.kisio.navitia.sdk.ui.journey.core.JourneyConfiguration;
import com.kisio.navitia.sdk.ui.journey.core.JourneyConfigurationLineMode;
import com.kisio.navitia.sdk.ui.journey.core.JourneyConfigurationProvider;
import com.kisio.navitia.sdk.ui.journey.core.JourneyConfigurationRoot;
import com.kisio.navitia.sdk.ui.journey.core.JourneyUI;
import com.kisio.navitia.sdk.ui.journey.core.JourneysRequest;
import com.kisio.navitia.sdk.ui.journey.core.PhysicalMode;
import com.kisio.navitia.sdk.ui.journey.core.TransportMode;
import com.kisio.navitia.sdk.ui.journey.presentation.ui.form.FormFragment;
import com.kisio.navitia.sdk.ui.journey.presentation.ui.journeys.JourneysFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import kotlin.Pair;
import kotlin.Unit;

public class JourneyUIActivity extends AppCompatActivity {

    public static final String DESTINATION = "arg:destination";
    public static final String JOURNEYS_REQUEST = "arg:JourneysRequest";
    public static final String ORIGIN = "arg:origin";
    public static final String WITH_FORM = "arg:withForm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE);
        String config = sharedPreferences.getString("navitia_raw_config", "");

        boolean withJourney;
        try {
            JSONObject configJson = new JSONObject(config);
            init(configJson);
            withJourney = configJson.optBoolean("isFormEnabled", false);
        } catch (JSONException e) {
            e.printStackTrace();

            withJourney = getIntent().getBooleanExtra(WITH_FORM, false);
        }

        super.onCreate(null);

        String applicationPackageName = getApplicationContext().getPackageName();
        int layoutResourceId = getApplicationContext().getResources()
          .getIdentifier("activity_journey_ui", "layout", applicationPackageName);
        setContentView(layoutResourceId);
        JourneyUI.Companion.getInstance().attachActivity(this);

        Intent intent = getIntent();
        if (intent != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment f;
            String tag;

            if (withJourney) {
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

            int contentResourceId = getApplicationContext().getResources()
              .getIdentifier("activity_journey_ui_content", "id", applicationPackageName);
            ft.replace(contentResourceId, f, tag);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        JourneyUI.Companion.getInstance().getDelegate().onBackPressed();
    }

    private void init(JSONObject config) {
      String token = config.optString("token");
      if (token.isEmpty()) {
        return;
      }

      String coverage = config.optString("coverage", "");
      if (coverage.isEmpty()) {
        return;
      }

      // Colors
      String primaryColor = config.optString("primaryColor", "");
      String secondaryColor = config.optString("secondaryColor", primaryColor);
      JourneyColors colors = new JourneyColors(primaryColor, secondaryColor);

      String originColor = config.optString("originColor", "");
      if (!originColor.isEmpty()) {
        colors.originColor(originColor);
      }
      String originIconColor = config.optString("originIconColor", "");
      if (!originIconColor.isEmpty()) {
        colors.originIconColor(originIconColor);
      }
      String originBackgroundColor = config.optString("originBackgroundColor", "");
      if (!originBackgroundColor.isEmpty()) {
        colors.originBackgroundColor(originBackgroundColor);
      }
      String destinationColor = config.optString("destinationColor", "");
      if (!destinationColor.isEmpty()) {
        colors.destinationColor(destinationColor);
      }
      String destinationIconColor = config.optString("destinationIconColor", "");
      if (!destinationIconColor.isEmpty()) {
        colors.destinationIconColor(destinationIconColor);
      }
      String destinationBackgroundColor = config.optString("destinationBackgroundColor", "");
      if (!destinationBackgroundColor.isEmpty()) {
        colors.destinationBackgroundColor(destinationBackgroundColor);
      }

      // Transport configuration
      String transportConfigurationString = config.optString("transportConfiguration", "");
      Gson gson = new Gson();
      JourneyConfigurationRoot journeyConfigurationRoot = gson.fromJson(transportConfigurationString,
        JourneyConfigurationRoot.class);
      List<JourneyConfigurationLineMode> transportLinesConfiguration = journeyConfigurationRoot == null ? new ArrayList<>() : journeyConfigurationRoot.getLines();
      List<JourneyConfigurationLineMode> transportModesConfiguration = journeyConfigurationRoot == null ? new ArrayList<>() : journeyConfigurationRoot.getModes();
      List<JourneyConfigurationProvider> transportProvidersConfiguration = journeyConfigurationRoot == null ? new ArrayList<>() : journeyConfigurationRoot.getProviders();
      JourneyConfiguration journeyConfiguration = new JourneyConfiguration(
        transportLinesConfiguration,
        transportModesConfiguration,
        transportProvidersConfiguration
      );

      // Options
      String disruptionContributor = config.optString("disruptionContributor", "");
      JourneyUI.Companion.getInstance().disruptionContributor(disruptionContributor);

      int maxHistory = config.optInt("maxHistory", 10);
      JourneyUI.Companion.getInstance().maxHistory(maxHistory);

      List<TransportMode> transportModes = toTransportModes(config.optJSONArray("transportModes"));
      JourneyUI.Companion.getInstance().transportModes(transportModes);

      boolean withEarlierLaterFeature = config.optBoolean("isEarlierLaterFeatureEnabled", false);
      if (withEarlierLaterFeature) {
        JourneyUI.Companion.getInstance().withEarlierLater();
      }

      boolean withMultiNetwork = config.optBoolean("isMultiNetworkEnabled", false);
      if (withMultiNetwork) {
        JourneyUI.Companion.getInstance().withMultiNetwork();
      }

      boolean withNextDepartures = config.optBoolean("isNextDeparturesFeatureEnabled", false);
      if (withNextDepartures) {
        JourneyUI.Companion.getInstance().withNextDepartures();
      }

      // Titles
      JSONObject customTitles = config.optJSONObject("customTitles");
      if (customTitles != null) {
        // Form screen
        String formTitleResId = customTitles.optString("form", "journeys");
        JourneyUI.Companion.getInstance()
          .formTitleRes(getStringResourceID(this, formTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));

        // Journeys screen
        String journeysTitleResId = customTitles.optString("journeys", "journeys");
        JourneyUI.Companion.getInstance()
          .journeysTitleRes(getStringResourceID(this, journeysTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));

        // Roadmap screen
        String roadmapTitleResId = customTitles.optString("roadmap", "roadmap");
        JourneyUI.Companion.getInstance()
          .roadmapTitleRes(getStringResourceID(this, roadmapTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.roadmap));

        // Ridesharing offers screen
        String ridesharingTitleResId = customTitles.optString("ridesharing", "ridesharing_noun");
        JourneyUI.Companion.getInstance()
          .ridesharingTitleRes(getStringResourceID(this, ridesharingTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.ridesharing_noun));

        // Autocomplete screen
        String autocompleteTitleResId = customTitles.optString("autocomplete", "journeys");
        JourneyUI.Companion.getInstance()
          .autoCompleteTitle(getStringResourceID(this, autocompleteTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));
      }

      // Environment
      String environment = config.optString("environment", "PROD");

      // Initialization
      JourneyUI.Companion.getInstance().init(
        this, // context
        colors, // colors
        coverage, // coverage
        token,  // token
        journeyConfiguration, // config
        null, // config file
        toExpertEnvironment(environment), // env
        (nav -> Unit.INSTANCE), // onNavigate
        (nav -> Unit.INSTANCE) // onBack
      );
    }

  private int getStringResourceID(Context context, String resId, int fallbackStringId) {
    String applicationPackageName = context.getPackageName();
    int requestedResourceId = context.getResources()
      .getIdentifier(resId, "string", applicationPackageName);
    return requestedResourceId > 0 ? requestedResourceId : fallbackStringId;
  }

  private ExpertEnvironment toExpertEnvironment(String environment) {
    switch (environment) {
      case "CUSTOMER":
        return ExpertEnvironment.CUSTOMER;
      case "DEV":
        return ExpertEnvironment.DEV;
      case "INTERNAL":
        return ExpertEnvironment.INTERNAL;
      default:
        return ExpertEnvironment.PROD;
    }
  }

  private ArrayList<TransportMode> toTransportModes(JSONArray array) {
    ArrayList<TransportMode> transportModes = new ArrayList<>();
    if (array == null || array.length() == 0) {
      return transportModes;
    }

    try {
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = (JSONObject) array.get(i);
        HashSet<PhysicalMode> physicalModes = new HashSet<>();
        JSONArray physicalModesArray = object.optJSONArray("physicalMode");
        for (int j=0; j < physicalModesArray.length(); j++) {
          physicalModes.add(getPhysicalMode(physicalModesArray.getString(j)));
        }

        TransportMode transportMode = new TransportMode(
          object.optString("title"), // title
          android.R.string.untitled, // titleRes
          getIcon(object.optString("icon")), // iconRes
          //toSet(object.optJSONArray("firstSectionMode")), // firstSectionModes
          new HashSet<>(), // firstSectionModes
          //toSet(object.optJSONArray("lastSectionMode")), // lastSectionMode
          new HashSet<>(), // lastSectionMode
          new HashSet<>(), // directPathMode
          //toPhysicalModeSet(object.optJSONArray("physicalMode")), // physicalModes
          physicalModes, // physicalModes
          object.optBoolean("realTime", false), // isRealTime
          object.optBoolean("selected", false) // isSelected
        );
        transportModes.add(transportMode);
      }
    } catch (JSONException e) {
      Log.e("JourneyUIActivity", e.getMessage());
    }

    return transportModes;
  }

  private PhysicalMode getPhysicalMode(String physicalMode) {
    for (int i = 0; i < PhysicalMode.values().length; i++) {
      if (PhysicalMode.values()[i].getBasePhysicalMode().equals(physicalMode)) {
        return PhysicalMode.values()[i];
      }
    }

    return PhysicalMode.AIR;
  }

  private int getIcon(@JourneysUICordovaPlugin.TransportModeIcon String value) {
    if (value.isEmpty()) {
      return -1;
    }

    switch (value) {
      case JourneysUICordovaPlugin.TransportModeIcon.BIKE:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_bike;
      case JourneysUICordovaPlugin.TransportModeIcon.BSS:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_bike_sharing_service;
      case JourneysUICordovaPlugin.TransportModeIcon.BUS:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_bus;
      case JourneysUICordovaPlugin.TransportModeIcon.CAR:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_car;
      case JourneysUICordovaPlugin.TransportModeIcon.COACH:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_coach;
      case JourneysUICordovaPlugin.TransportModeIcon.FERRY:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_ferry;
      case JourneysUICordovaPlugin.TransportModeIcon.FUNICULAR:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_funicular;
      case JourneysUICordovaPlugin.TransportModeIcon.LOCALTRAIN:
      case JourneysUICordovaPlugin.TransportModeIcon.LONGDISTANCETRAIN:
      case JourneysUICordovaPlugin.TransportModeIcon.RAPIDTRANSIT:
      case JourneysUICordovaPlugin.TransportModeIcon.TRAIN:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_train;
      case JourneysUICordovaPlugin.TransportModeIcon.METRO:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_metro;
      case JourneysUICordovaPlugin.TransportModeIcon.RIDESHARING:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_ridesharing;
      case JourneysUICordovaPlugin.TransportModeIcon.SHUTTLE:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_shuttle;
      case JourneysUICordovaPlugin.TransportModeIcon.TAXI:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_taxi;
      case JourneysUICordovaPlugin.TransportModeIcon.TRAMWAY:
        return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_tramway;
      default:
        return -1;
    }
  }
}
