package com.kisio.navitia.sdk.ui.journey.cordova;

import androidx.annotation.StringDef;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        String BIKE = "physical_mode_bike";
        String BSS = "physical_mode_bss";
        String BUS = "physical_mode_bus";
        String CAR = "physical_mode_car";
        String COACH = "physical_mode_coach";
        String FERRY = "physical_mode_ferry";
        String FUNICULAR = "physical_mode_funicular";
        String LOCALTRAIN = "physical_mode_localtrain";
        String LONGDISTANCETRAIN = "physical_mode_longdistancetrain";
        String METRO = "physical_mode_metro";
        String RAPIDTRANSIT = "physical_mode_rapidtransit";
        String RIDESHARING = "section_mode_ridesharing";
        String SHUTTLE = "physical_mode_shuttle";
        String TAXI = "physical_mode_taxi";
        String TRAIN = "physical_mode_train";
        String TRAMWAY = "physical_mode_tramway";
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

        // Use form or not
        this.withJourney = config.optBoolean("isFormEnabled", false);

        // Titles
        JSONObject customTitles = config.optJSONObject("customTitles");
        if (customTitles != null) {
            // Form screen
            String formTitleResId = customTitles.optString("form", "journeys");
            JourneyUI.Companion.getInstance()
              .formTitleRes(getStringResourceID(cordova.getContext(), formTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));

            // Journeys screen
            String journeysTitleResId = customTitles.optString("journeys", "journeys");
            JourneyUI.Companion.getInstance()
              .journeysTitleRes(getStringResourceID(cordova.getContext(), journeysTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));

            // Roadmap screen
            String roadmapTitleResId = customTitles.optString("roadmap", "roadmap");
            JourneyUI.Companion.getInstance()
              .roadmapTitleRes(getStringResourceID(cordova.getContext(), roadmapTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.roadmap));

            // Ridesharing offers screen
            String ridesharingTitleResId = customTitles.optString("ridesharing", "ridesharing_noun");
            JourneyUI.Companion.getInstance()
              .ridesharingTitleRes(getStringResourceID(cordova.getContext(), ridesharingTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.ridesharing_noun));

            // Autocomplete screen
            String autocompleteTitleResId = customTitles.optString("autocomplete", "journeys");
            JourneyUI.Companion.getInstance()
              .autoCompleteTitle(getStringResourceID(cordova.getContext(), autocompleteTitleResId, com.kisio.navitia.sdk.ui.journey.R.string.journeys));
        }

        // Environment
        String environment = config.optString("environment", "PROD");

        // Initialization
        JourneyUI.Companion.getInstance().init(
            this.cordova.getActivity().getApplicationContext(), // context
            colors, // colors
            coverage, // coverage
            token,  // token
          journeyConfiguration, // config
            null, // config file
            toExpertEnvironment(environment), // env
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
            JourneysRequest.TravelerType travelerType = JourneysRequest.TravelerType.STANDARD;
            if (params.has("travelerType")) {
                travelerType = toTravelerType(params.getString("travelerType"));
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
                travelerType // travelerType
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
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_bike;
            case TransportModeIcon.BSS:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_bike_sharing_service;
            case TransportModeIcon.BUS:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_bus;
            case TransportModeIcon.CAR:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_car;
            case TransportModeIcon.COACH:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_coach;
            case TransportModeIcon.FERRY:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_ferry;
            case TransportModeIcon.FUNICULAR:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_funicular;
            case TransportModeIcon.LOCALTRAIN:
            case TransportModeIcon.LONGDISTANCETRAIN:
            case TransportModeIcon.RAPIDTRANSIT:
            case TransportModeIcon.TRAIN:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_train;
            case TransportModeIcon.METRO:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_metro;
            case TransportModeIcon.RIDESHARING:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_connection_mode_ridesharing;
            case TransportModeIcon.SHUTTLE:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_shuttle;
            case TransportModeIcon.TAXI:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_taxi;
            case TransportModeIcon.TRAMWAY:
                return com.kisio.navitia.sdk.engine.design.R.drawable.ic_physical_mode_tramway;
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
            Log.e(TAG, e.getMessage());
        }

        return transportModes;
    }

    private JourneysRequest.TravelerType toTravelerType(String value) {
        JourneysRequest.TravelerType travelerType = JourneysRequest.TravelerType.STANDARD;
        switch (value) {
            case "fast_walker":
                travelerType = JourneysRequest.TravelerType.FAST;
                break;
            case "luggage":
                travelerType = JourneysRequest.TravelerType.LUGGAGE;
                break;
            case "slow_walker":
                travelerType = JourneysRequest.TravelerType.SLOW;
                break;
            case "standard":
                travelerType = JourneysRequest.TravelerType.STANDARD;
                break;
            case "wheelchair":
                travelerType = JourneysRequest.TravelerType.WHEELCHAIR;
                break;
        }

        return travelerType;
    }

    private int getStringResourceID(Context context, String resId, int fallbackStringId) {
        String applicationPackageName = context.getApplicationContext().getPackageName();
        int requestedResourceId = context.getResources()
          .getIdentifier(resId, "string", applicationPackageName);
        return requestedResourceId > 0 ? requestedResourceId : fallbackStringId;
    }

    private PhysicalMode getPhysicalMode(String physicalMode) {
        for (int i = 0; i < PhysicalMode.values().length; i++) {
            if (PhysicalMode.values()[i].getBasePhysicalMode().equals(physicalMode)) {
                return PhysicalMode.values()[i];
            }
        }

        return PhysicalMode.AIR;
    }
}
