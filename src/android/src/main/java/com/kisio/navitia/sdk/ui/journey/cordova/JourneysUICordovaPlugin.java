package com.kisio.navitia.sdk.ui.journey.cordova;

import androidx.annotation.StringDef;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.kisio.navitia.sdk.ui.journey.core.enums.TransportMode;
import com.kisio.navitia.sdk.ui.journey.core.JourneysUI;
import com.kisio.navitia.sdk.ui.journey.core.JourneysRequest;
import com.kisio.navitia.sdk.ui.journey.core.cordova.JourneysUIActivity;
import com.kisio.navitia.sdk.ui.journey.presentation.model.TransportModeModel;
import com.kisio.navitia.sdk.ui.journey.util.Constant;
import com.kisio.navitia.sdk.ui.journey.util.NavitiaSDKPreferencesManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneysUICordovaPlugin extends CordovaPlugin {

    private Map<String, Action> actions = new HashMap<String, Action>();
    private ArrayList<TransportModeModel> transportModes = new ArrayList<>();
    private boolean formJourney = false;

    private static final String TAG = JourneysUICordovaPlugin.class.getName();

    private interface IAction {
        void doAction(JSONObject params, CallbackContext callbackContext);
    }

    private abstract class Action implements IAction {
        @Override
        public abstract void doAction(JSONObject params, CallbackContext callbackContext);
    }

    @StringDef({})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TransportModeIcon {
        String BIKE = "bike";
        String BSS = "bss";
        String BUS = "bus";
        String CAR = "car";
        String CARPOOLING = "carpooling";
        String COACH = "coach";
        String FERRY = "ferry";
        String FUNICULAR = "funicular";
        String LOCALTRAIN = "localtrain";
        String LONGDISTANCETRAIN = "longdistancetrain";
        String METRO = "metro";
        String RAPIDTRANSIT = "rapidtransit";
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
            actions.get(action).doAction(args.getJSONObject(0), callbackContext);
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

        JourneysUI.getInstance().token(token);

        String mainColor = config.optString("mainColor", "#40958E");
        JourneysUI.getInstance().mainColor(mainColor);

        String accentColor = config.optString("accentColor", "#40958E");
        JourneysUI.getInstance().accentColor(accentColor);

        String originIconColor = config.optString("originIconColor", "#00BB75");
        JourneysUI.getInstance().originIconColor(originIconColor);

        String originBackgroundColor = config.optString("originBackgroundColor", "#00BB75");
        JourneysUI.getInstance().originBackgroundColor(originBackgroundColor);

        String destinationIconColor = config.optString("destinationIconColor", "#B00353");
        JourneysUI.getInstance().destinationIconColor(destinationIconColor);

        String destinationBackgroundColor = config.optString("destinationBackgroundColor", "#B00353");
        JourneysUI.getInstance().destinationBackgroundColor(destinationBackgroundColor);

        boolean multiNetwork = config.optBoolean("multiNetwork", false);
        if (multiNetwork) {
            JourneysUI.getInstance().withMultiNetwork();
        }

        boolean isEarlierLaterFeatureEnabled = config.optBoolean("isEarlierLaterFeatureEnabled", false);
        if (isEarlierLaterFeatureEnabled) {
            JourneysUI.getInstance().withEarlierLaterFeature();
        }

        boolean isNextDeparturesFeatureEnabled = config.optBoolean("isNextDeparturesFeatureEnabled", false);
        if (isNextDeparturesFeatureEnabled) {
            JourneysUI.getInstance().withNextDepartures();
        }
        
        this.transportModes = getTransportModes(config.optJSONArray("modeForm"));
        this.formJourney = config.optBoolean("formJourney", false);
        callbackContext.success();
    }

    private void invokeJourneyResults(JSONObject params, CallbackContext callbackContext) {
        try {
            final Context context = this.cordova.getActivity().getApplicationContext();
            final JourneysRequest request = new JourneysRequest(params.getString("coverage"));

            if (params.has("originId")) {
                request.setOriginId(params.getString("originId"));
            }
            if (params.has("originLabel")) {
                request.setOriginLabel(params.getString("originLabel"));
            }
            if (params.has("destinationId")) {
                request.setDestinationId(params.getString("destinationId"));
            }
            if (params.has("destinationLabel")) {
                request.setDestinationLabel(params.getString("destinationLabel"));
            }
            if (params.has("datetime")) {
                request.setDatetime(getDatetimeFromString(params.getString("datetime")));
            }
            if (params.has("datetimeRepresents")) {
                request.setDatetimeRepresents(params.getString("datetimeRepresents"));
            }
            if (params.has("forbiddenUris")) {
                request.setForbiddenUris(getStringListFromJsonArray(params.getJSONArray("forbiddenUris")));
            }
            if (params.has("firstSectionModes")) {
                request.setFirstSectionModes(getStringListFromJsonArray(params.getJSONArray("firstSectionModes")));
            }
            if (params.has("lastSectionModes")) {
                request.setLastSectionModes(getStringListFromJsonArray(params.getJSONArray("lastSectionModes")));
            }
            if (params.has("count")) {
                request.setCount(params.getInt("count"));
            }
            if (params.has("minNbJourneys")) {
                request.setMinNbJourneys(params.getInt("minNbJourneys"));
            }
            if (params.has("maxNbJourneys")) {
                request.setMaxNbJourneys(params.getInt("maxNbJourneys"));
            }
            if (params.has("addPoiInfos")) {
                request.setAddPoiInfos(getStringListFromJsonArray(params.getJSONArray("addPoiInfos")));
            } else {
                List<String> addPoiInfosList = new ArrayList<>();
                for (TransportModeModel transportMode : transportModes) {
                    if (transportMode.getTitle().equalsIgnoreCase("bss") && transportMode.isRealTime()) {
                        addPoiInfosList.add("bss_stands");
                    }

                    if (transportMode.getTitle().equalsIgnoreCase("car") && transportMode.isRealTime()) {
                        addPoiInfosList.add("car_park");
                    }
                }

                if (addPoiInfosList.size() > 0) {
                    request.setAddPoiInfos(addPoiInfosList);
                }
            }
            if (params.has("directPath")) {
                request.setDirectPath(params.getString("directPath"));
            }
            request.setTransportModeListRequested(this.transportModes);

            final Intent intent = new Intent(context, JourneysUIActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.JOURNEYS_REQUEST, request);
            intent.putExtra(Constant.WITH_FORM, formJourney);

            context.startActivity(intent);

            callbackContext.success();
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void resetPreferences(CallbackContext callbackContext) {
        final Context context = this.cordova.getActivity().getApplicationContext();
        NavitiaSDKPreferencesManager.resetPreferences(context);

        callbackContext.success();
    }

    private DateTime getDatetimeFromString(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            DateTime parsedDate = formatter.parseDateTime(value);

            return parsedDate;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            DateTime currentDate = new DateTime();

            return currentDate;
        }
    }

    private List<String> getStringListFromJsonArray(JSONArray array) {
        List<String> stringList = new ArrayList<String>();
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

    private ArrayList<TransportModeModel> getTransportModes(JSONArray array) {
        ArrayList<TransportModeModel> transportModes = new ArrayList<>();
        if (array == null || array.length() == 0) {
            return transportModes;
        }

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);

                TransportModeModel transportModeModel = new TransportModeModel();
                transportModeModel.setTitle(object.optString("title"));              
                transportModeModel.setResIconId(getIcon(object.optString("icon")));
                transportModeModel.setFirstSectionMode(getStringListFromJsonArray(object.optJSONArray("firstSectionMode")).toArray(new String[0]));
                transportModeModel.setLastSectionMode(getStringListFromJsonArray(object.optJSONArray("lastSectionMode")).toArray(new String[0]));
                transportModeModel.setPhysicalModes(getStringListFromJsonArray(object.optJSONArray("physicalMode")).toArray(new String[0]));
                transportModeModel.setRealTime(object.optBoolean("realTime", false));
                transportModeModel.setSelected(object.optBoolean("selected", false));

                transportModes.add(transportModeModel);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return transportModes;
    }

    private int getIcon(@TransportModeIcon String value) {
        if (TextUtils.isEmpty(value)) {
            return -1;
        }

        switch (value) {
            case TransportModeIcon.BIKE:
                return TransportMode.BIKE.getResIconId();
            case TransportModeIcon.BSS:
                return TransportMode.BSS.getResIconId();
            case TransportModeIcon.BUS:
                return TransportMode.BUS.getResIconId();
            case TransportModeIcon.CAR:
                return TransportMode.CAR.getResIconId();
            case TransportModeIcon.CARPOOLING:
                return TransportMode.CARPOOLING.getResIconId();
            case TransportModeIcon.COACH:
                return TransportMode.COACH.getResIconId();
            case TransportModeIcon.FERRY:
                return TransportMode.FERRY.getResIconId();
            case TransportModeIcon.FUNICULAR:
                return TransportMode.FUNICULAR.getResIconId();
            case TransportModeIcon.LOCALTRAIN:
                return TransportMode.LOCAL_TRAIN.getResIconId();
            case TransportModeIcon.LONGDISTANCETRAIN:
                return TransportMode.LONG_DISTANCE_TRAIN.getResIconId();
            case TransportModeIcon.METRO:
                return TransportMode.METRO.getResIconId();
            case TransportModeIcon.RAPIDTRANSIT:
                return TransportMode.RAPID_TRANSIT.getResIconId();
            case TransportModeIcon.SHUTTLE:
                return TransportMode.SHUTTLE.getResIconId();
            case TransportModeIcon.TAXI:
                return TransportMode.TAXI.getResIconId();
            case TransportModeIcon.TRAIN:
                return TransportMode.TRAIN.getResIconId();
            case TransportModeIcon.TRAMWAY:
                return TransportMode.TRAMWAY.getResIconId();
            default:
                return -1;
        }
    }
}
