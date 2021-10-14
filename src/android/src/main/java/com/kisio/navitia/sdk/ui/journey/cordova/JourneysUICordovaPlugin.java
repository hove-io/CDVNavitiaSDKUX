package com.kisio.navitia.sdk.ui.journey.cordova;

import androidx.annotation.StringDef;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.kisio.navitia.sdk.ui.journey.core.ExpertEnvironment;
import com.kisio.navitia.sdk.ui.journey.core.JourneyColors;
import com.kisio.navitia.sdk.ui.journey.core.JourneyUI;
import com.kisio.navitia.sdk.ui.journey.core.JourneysRequest;
import com.kisio.navitia.sdk.ui.journey.core.PhysicalMode;
import com.kisio.navitia.sdk.ui.journey.core.TransportMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kotlin.Pair;
import kotlin.Unit;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JourneysUICordovaPlugin extends CordovaPlugin {

    private final Map<String, Action> actions = new HashMap<>();
    private boolean withJourney = false;

    private static final String TAG = JourneysUICordovaPlugin.class.getName();

    private interface IAction {
        void doAction(JSONObject params, CallbackContext callbackContext);
    }

    private abstract class Action implements IAction {
        @Override
        public abstract void doAction(JSONObject params, CallbackContext callbackContext);
    }

    @StringDef()
    @Retention(RetentionPolicy.SOURCE)
    public @interface TransportModeIcon {
        String BIKE = "bike";
        String BSS = "bss";
        String BUS = "bus";
        String CAR = "car";
        String COACH = "coach";
        String FERRY = "ferry";
        String FUNICULAR = "funicular";
        String LOCALTRAIN = "localtrain";
        String LONGDISTANCETRAIN = "longdistancetrain";
        String METRO = "metro";
        String RAPIDTRANSIT = "rapidtransit";
        String RIDESHARING = "ridesharing";
        String SHUTTLE = "shuttle";
        String TAXI = "taxi";
        String TRAIN = "train";
        String TRAMWAY = "tramway";
    }

    public JourneysUICordovaPlugin() {
        actions.put("init", new Action() {
            @Override
            public void doAction(JSONObject config, CallbackContext callbackContext) {
                init(config, callbackContext);
            }
        });
        actions.put("invokeJourneyResults", new Action() {
            @Override
            public void doAction(JSONObject params, CallbackContext callbackContext) {
                invokeJourneyResults(params, callbackContext);
            }
        });
        actions.put("resetPreferences", new Action() {
            @Override
            public void doAction(JSONObject params, CallbackContext callbackContext) {
                resetPreferences(callbackContext);
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (actions.containsKey(action)) {
            Objects.requireNonNull(actions.get(action)).doAction(args.getJSONObject(0), callbackContext);
        } else {
            callbackContext.error("Action " + action + " not found");
        }

        return true;
    }

    private void init(JSONObject config, CallbackContext callbackContext) {
        String token = config.optString("token");
        if (token.isEmpty()) {
            callbackContext.error("No token specified");
            return;
        }

        String coverage = config.optString("coverage", "");
        if (coverage.isEmpty()) {
            callbackContext.error("No coverage specified");
            return;
        }

        // Colors
        String primaryColor = config.optString("primaryColor", "");
        String secondaryColor = config.optString("secondaryColor", "");
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

        // Options
        String disruptionContributor = config.optString("disruptionContributor", "");
        JourneyUI.Companion.getInstance().disruptionContributor(disruptionContributor);

        int maxHistory = config.optInt("maxHistory", 10);
        JourneyUI.Companion.getInstance().maxHistory(maxHistory);

        List<TransportMode> transportModes = toTransportModes(config.optJSONArray("modeForm"));
        JourneyUI.Companion.getInstance().transportModes(transportModes);

        boolean withEarlierLaterFeature = config.optBoolean("isEarlierLaterFeatureEnabled", false);
        if (withEarlierLaterFeature) {
            JourneyUI.Companion.getInstance().withEarlierLater();
        }

        boolean withMultiNetwork = config.optBoolean("multiNetwork", false);
        if (withMultiNetwork) {
            JourneyUI.Companion.getInstance().withMultiNetwork();
        }

        boolean withNextDepartures = config.optBoolean("isNextDeparturesFeatureEnabled", false);
        if (withNextDepartures) {
            JourneyUI.Companion.getInstance().withNextDepartures();
        }

        // Use form or not
        this.withJourney = config.optBoolean("formJourney", false);

        // Titles
        JSONObject customTitles = config.optJSONObject("customTitles", null);
        if (customTitles != null) {
            // Form screen
            String formTitleResId = customTitles.optString("form", "journeys");
            JourneyUI.Companion.getInstance()
              .formTitleRes(getStringResourceID(cordova.getContext(), formTitleResId, R.string.journeys));
      
            // Journeys screen
            String journeysTitleResId = customTitles.optString("journeys", "journeys");
            JourneyUI.Companion.getInstance()
              .journeysTitleRes(getStringResourceID(cordova.getContext(), journeysTitleResId, R.string.journeys));
      
            // Roadmap screen
            String roadmapTitleResId = customTitles.optString("roadmap", "roadmap");
            JourneyUI.Companion.getInstance()
              .roadmapTitleRes(getStringResourceID(cordova.getContext(), roadmapTitleResId, R.string.roadmap));
      
            // Ridesharing offers screen
            String ridesharingTitleResId = customTitles.optString("ridesharing", "ridesharing_noun");
            JourneyUI.Companion.getInstance()
              .ridesharingTitleRes(getStringResourceID(cordova.getContext(), ridesharingTitleResId, R.string.ridesharing_noun));
      
            // Autocomplete screen
            String autocompleteTitleResId = customTitles.optString("autocomplete", "journeys");
            JourneyUI.Companion.getInstance()
              .autoCompleteTitle(getStringResourceID(cordova.getContext(), autocompleteTitleResId, R.string.journeys));
        }

        // Initialization
        JourneyUI.Companion.getInstance().init(
            this.cordova.getActivity().getApplicationContext(), // context
            colors, // colors
            coverage, // coverage
            token,  // token
            null, // config
            null, // config file
            toExpertEnvironment(config.optString("environment", "PROD")), // env
            (nav -> Unit.INSTANCE), // onNavigate
            (nav -> Unit.INSTANCE) // onBack
        );

        callbackContext.success();
    }

    private void invokeJourneyResults(JSONObject params, CallbackContext callbackContext) {
        try {
            final Context context = this.cordova.getActivity().getApplicationContext();

            String originId = "";
            if (params.has("originId")) {
                originId = params.getString("originId");
            }
            String originLabel = "";
            if (params.has("originLabel")) {
                originLabel = params.getString("originLabel");
            }
            String destinationId = "";
            if (params.has("destinationId")) {
                destinationId = params.getString("destinationId");
            }
            String destinationLabel = "";
            if (params.has("destinationLabel")) {
                destinationLabel = params.getString("destinationLabel");
            }
            DateTime datetime = DateTime.now();
            if (params.has("datetime")) {
                datetime = toDateTime(params.getString("datetime"));
            }
            JourneysRequest.DateTimeRepresents datetimeRepresents = JourneysRequest.DateTimeRepresents.DEPARTURE;
            if (params.has("datetimeRepresents")) {
                datetimeRepresents = toDateTimeRepresents(params.getString("datetimeRepresents"));
            }
            Set<PhysicalMode> forbiddenUris = new HashSet<>();
            if (params.has("forbiddenUris")) {
                forbiddenUris = toPhysicalModeSet(params.getJSONArray("forbiddenUris"));
            }
            Set<String> firstSectionModes = new HashSet<>();
            if (params.has("firstSectionModes")) {
                firstSectionModes = toSet(params.getJSONArray("firstSectionModes"));
            }
            Set<String> lastSectionModes = new HashSet<>();
            if (params.has("lastSectionModes")) {
                lastSectionModes = toSet(params.getJSONArray("lastSectionModes"));
            }
            int count = -1;
            if (params.has("count")) {
                count = params.getInt("count");
            }
            int minNbJourneys = -1;
            if (params.has("minNbJourneys")) {
                minNbJourneys = params.getInt("minNbJourneys");
            }
            int maxNbJourneys = -1;
            if (params.has("maxNbJourneys")) {
                maxNbJourneys = params.getInt("maxNbJourneys");
            }
            Set<String> addPoiInfos = new HashSet<>();
            if (params.has("addPoiInfos")) {
                addPoiInfos = toSet(params.getJSONArray("addPoiInfos"));
            }
            Set<String> directPathMode = new HashSet<>();
            /*if (params.has("directPathMode")) {
                directPathMode.add(params.getString("directPathMode"));
            }*/
            String directPath = "";
            if (params.has("directPath")) {
                directPath = params.getString("directPath");
            }

            final JourneysRequest request = new JourneysRequest(
                new ArrayList<>(), // allowedId
                addPoiInfos, // addPoiInfos
                count, // count
                JourneysRequest.DataFreshness.BASE_SCHEDULE, // dataFreshness
                datetime, // dateTime
                datetimeRepresents, // dateTimeRepresents
                "", // destinationAddress
                destinationId, // destinationId
                destinationLabel, // destinationLabel
                directPathMode, // directPathMode
                firstSectionModes, // firstSectionModes
                forbiddenUris, // forbiddenUris
                lastSectionModes, // lastSectionModes
                maxNbJourneys, // maxJourneys
                minNbJourneys, // minJourneys
                "", // originAddress
                originId, // originId
                originLabel, // originLabel
                new HashSet<>(), // physicalModes
                JourneysRequest.TravelerType.STANDARD // travelerType
            );

            final Intent intent = new Intent(context, JourneyUIActivity.class);
            intent.putExtra(JourneyUIActivity.DESTINATION, new Pair<>(destinationId, destinationLabel));
            intent.putExtra(JourneyUIActivity.JOURNEYS_REQUEST, request);
            intent.putExtra(JourneyUIActivity.ORIGIN, new Pair<>(originId, originLabel));
            intent.putExtra(JourneyUIActivity.WITH_FORM, withJourney);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

            callbackContext.success();
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private int getIcon(@TransportModeIcon String value) {
        if (value.isEmpty()) {
            return -1;
        }

        switch (value) {
            case TransportModeIcon.BIKE:
                return R.drawable.ic_connection_mode_bike;
            case TransportModeIcon.BSS:
                return R.drawable.ic_connection_mode_bike_sharing_service;
            case TransportModeIcon.BUS:
                return R.drawable.ic_physical_mode_bus;
            case TransportModeIcon.CAR:
                return R.drawable.ic_connection_mode_car;
            case TransportModeIcon.COACH:
                return R.drawable.ic_physical_mode_coach;
            case TransportModeIcon.FERRY:
                return R.drawable.ic_physical_mode_ferry;
            case TransportModeIcon.FUNICULAR:
                return R.drawable.ic_physical_mode_funicular;
            case TransportModeIcon.LOCALTRAIN:
            case TransportModeIcon.LONGDISTANCETRAIN:
            case TransportModeIcon.RAPIDTRANSIT:
            case TransportModeIcon.TRAIN:
                return R.drawable.ic_physical_mode_train;
            case TransportModeIcon.METRO:
                return R.drawable.ic_physical_mode_metro;
            case TransportModeIcon.RIDESHARING:
                return R.drawable.ic_connection_mode_ridesharing;
            case TransportModeIcon.SHUTTLE:
                return R.drawable.ic_physical_mode_shuttle;
            case TransportModeIcon.TAXI:
                return R.drawable.ic_physical_mode_taxi;
            case TransportModeIcon.TRAMWAY:
                return R.drawable.ic_physical_mode_tramway;
            default:
                return -1;
        }
    }

    private void resetPreferences(CallbackContext callbackContext) {
        JourneyUI.Companion.getInstance().resetUserPreferences();

        callbackContext.success();
    }

    private DateTime toDateTime(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            return formatter.parseDateTime(value);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            return new DateTime();
        }
    }

    private JourneysRequest.DateTimeRepresents toDateTimeRepresents(String value) {
        JourneysRequest.DateTimeRepresents dateTimeRepresents = JourneysRequest.DateTimeRepresents.DEPARTURE;

        switch (value) {
            case "arrival":
                dateTimeRepresents = JourneysRequest.DateTimeRepresents.ARRIVAL;
                break;
            case "departure":
                dateTimeRepresents = JourneysRequest.DateTimeRepresents.DEPARTURE;
                break;
        }

        return dateTimeRepresents;
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

    private Set<PhysicalMode> toPhysicalModeSet(JSONArray array) {
        HashSet<PhysicalMode> physicalModeList = new HashSet<>();
        if (array == null || array.length() == 0) {
            return physicalModeList;
        }

        try {
            for (int i = 0; i < array.length(); i++) {
                PhysicalMode physicalMode = null;

                switch (array.getString(i)) {
                    case "physical_mode:Air":
                        physicalMode = PhysicalMode.AIR;
                        break;
                    case "physical_mode:Bike":
                        physicalMode = PhysicalMode.BIKE;
                        break;
                    case "physical_mode:BikeSharingService":
                        physicalMode = PhysicalMode.BIKE_SHARING_SERVICE;
                        break;
                    case "physical_mode:Boat":
                        physicalMode = PhysicalMode.BOAT;
                        break;
                    case "physical_mode:Bus":
                        physicalMode = PhysicalMode.BUS;
                        break;
                    case "physical_mode:BusRapidTransit":
                        physicalMode = PhysicalMode.BUS_RAPID_TRANSIT;
                        break;
                    case "physical_mode:Car":
                        physicalMode = PhysicalMode.CAR;
                        break;    
                    case "physical_mode:CheckIn":
                        physicalMode = PhysicalMode.CHECK_IN;
                        break;
                    case "physical_mode:CheckOut":
                        physicalMode = PhysicalMode.CHECK_OUT;
                        break;
                    case "physical_mode:Coach":
                        physicalMode = PhysicalMode.COACH;
                        break;
                    case "physical_mode:Ferry":
                        physicalMode = PhysicalMode.FERRY;
                        break;
                    case "physical_mode:Funicular":
                        physicalMode = PhysicalMode.FUNICULAR;
                        break;
                    case "physical_mode:LocalTrain":
                        physicalMode = PhysicalMode.LOCAL_TRAIN;
                        break;
                    case "physical_mode:LongDistanceTrain":
                        physicalMode = PhysicalMode.LONG_DISTANCE_TRAIN;
                        break;
                    case "physical_mode:Metro":
                        physicalMode = PhysicalMode.METRO;
                        break;
                    case "physical_mode:RailShuttle":
                        physicalMode = PhysicalMode.RAIL_SHUTTLE;
                        break;
                    case "physical_mode:RapidTransit":
                        physicalMode = PhysicalMode.RAPID_TRANSIT;
                        break;
                    case "physical_mode:Shuttle":
                        physicalMode = PhysicalMode.SHUTTLE;
                        break;
                    case "physical_mode:SuspendedCableCar":
                        physicalMode = PhysicalMode.SUSPENDED_CABLE_CAR;
                        break;
                    case "physical_mode:Taxi":
                        physicalMode = PhysicalMode.TAXI;
                        break;
                    case "physical_mode:Train":
                        physicalMode = PhysicalMode.TRAIN;
                        break;
                    case "physical_mode:Tramway":
                        physicalMode = PhysicalMode.TRAMWAY;
                        break;
                }

                if (physicalMode != null) {
                    physicalModeList.add(physicalMode);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return physicalModeList;
    }

    private Set<String> toSet(JSONArray array) {
        HashSet<String> stringList = new HashSet<>();
        if (array == null || array.length() == 0) {
            return stringList;
        }

        try {
            for (int i = 0; i < array.length(); i++) {
                stringList.add(array.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return stringList;
    }

    private ArrayList<TransportMode> toTransportModes(JSONArray array) {
        ArrayList<TransportMode> transportModes = new ArrayList<>();
        if (array == null || array.length() == 0) {
            return transportModes;
        }

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
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
                    new HashSet<PhysicalMode>(), // physicalModes
                    object.optBoolean("realTime", false), // isRealTime
                    object.optBoolean("selected", false) // isSelected
                );
                transportModes.add(transportMode);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return transportModes;
    }

    private int getStringResourceID(Context context, String resId, int fallbackStringId) {
        int requestedResourceId = context.getResources()
          .getIdentifier(resId, "string", BuildConfig.APPLICATION_ID);
        return requestedResourceId > 0 ? requestedResourceId : fallbackStringId;
    }
}
