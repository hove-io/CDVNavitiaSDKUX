package org.kisio.CDVNavitiaSDKUI;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kisio.navitia.sdk.ui.core.JourneysRequest;
import org.kisio.navitia.sdk.ui.presentation.form.FormActivity;
import org.kisio.navitia.sdk.ui.presentation.journeys.JourneysActivity;
import org.kisio.navitia.sdk.ui.presentation.model.TransportModeModel;
import org.kisio.navitia.sdk.ui.util.Configuration;
import org.kisio.navitia.sdk.ui.util.Constant;
import org.kisio.navitia.sdk.ui.util.NavitiaSDKPreferencesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CDVNavitiaSDKUI extends CordovaPlugin {

    private Map<String, Action> actions = new HashMap<String, Action>();

    private ArrayList<TransportModeModel> transportModes = new ArrayList<>();
    private boolean formJourney = false;

    private static final String TAG = CDVNavitiaSDKUI.class.getName();

    private interface IAction {
        void doAction(JSONObject params, CallbackContext callbackContext);
    }

    private abstract class Action implements IAction {
        @Override
        public abstract void doAction(JSONObject params, CallbackContext callbackContext);
    }

    public CDVNavitiaSDKUI() {
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

        Configuration.TOKEN = token;

        String mainColor = config.optString("mainColor", "#40958E");
        Configuration.setMainColor(mainColor);

        String originColor = config.optString("originColor", "#00BB75");
        Configuration.setOriginColor(originColor);

        String destinationColor = config.optString("destinationColor", "#B00353");
        Configuration.setDestinationColor(destinationColor);

        boolean multiNetwork = config.optBoolean("multiNetwork", false);
        Configuration.MULTI_NETWORK = multiNetwork;
        
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
            }
            if (params.has("directPath")) {
                request.setDirectPath(params.getString("directPath"));
            }

            final Intent intent = formJourney ? new Intent(context, FormActivity.class) : new Intent(context, JourneysActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.JOURNEYS_REQUEST, request);
            intent.putParcelableArrayListExtra(Constant.JOURNEYS_TRANSPORT_MODE, this.transportModes);

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
                transportModeModel.setTextIcon(object.optString("icon"));
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
}
