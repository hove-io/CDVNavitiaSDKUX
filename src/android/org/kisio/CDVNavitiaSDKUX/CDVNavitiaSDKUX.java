package org.kisio.CDVNavitiaSDKUX;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kisio.NavitiaSDKUX.Config.Configuration;
import org.kisio.NavitiaSDKUX.Controllers.JourneySolutionsActivity;
import org.kisio.NavitiaSDKUX.Controllers.JourneySolutionsInParameters;
import org.kisio.NavitiaSDKUX.NavitiaSDKUX;
import org.kisio.NavitiaSDKUX.Util.NavitiaSDKPreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CDVNavitiaSDKUX extends CordovaPlugin {

    private NavitiaSDKUX navitiaSDKUX;
    private Map<String, Action> actions = new HashMap<String, Action>();

    private static final String TAG = CDVNavitiaSDKUX.class.getName();

    private interface IAction {
        void doAction(JSONObject params, CallbackContext callbackContext);
    }

    private abstract class Action implements IAction {
        @Override
        public abstract void doAction(JSONObject params, CallbackContext callbackContext);
    }

    public CDVNavitiaSDKUX() {
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
        final String token = config.optString("token");
        if (token.isEmpty()) {
            callbackContext.error("No token specified");
            return;
        }
        Configuration.token = token;

        final String mainColor = config.optString("mainColor");
        if (!mainColor.isEmpty()) {
            Configuration.colors.setTertiary(Color.parseColor(mainColor));
        }
        final String departureColor = config.optString("departureColor");
        if (!departureColor.isEmpty()) {
            Configuration.colors.setOrigin(Color.parseColor(departureColor));
        }
        final String arrivalColor = config.optString("arrivalColor");
        if (!arrivalColor.isEmpty()) {
            Configuration.colors.setDestination(Color.parseColor(arrivalColor));
        }
        callbackContext.success();
    }

    private void invokeJourneyResults(JSONObject params, CallbackContext callbackContext) {
        final Context context = this.cordova.getActivity().getApplicationContext();
        final Intent intent = new Intent(context, JourneySolutionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            final JourneySolutionsInParameters journeyParameters = new JourneySolutionsInParameters(params.getString("originId"), params.getString("destinationId"));

            if (params.has("originLabel")) {
                journeyParameters.originLabel = params.getString("originLabel");
            }
            if (params.has("destinationLabel")) {
                journeyParameters.destinationLabel = params.getString("destinationLabel");
            }
            if (params.has("datetime")) {
                journeyParameters.datetime = getDatetimeFromString(params.getString("datetime"));
            }
            if (params.has("datetimeRepresents")) {
                journeyParameters.datetimeRepresents = params.getString("datetimeRepresents");
            }
            if (params.has("forbiddenUris")) {
                journeyParameters.forbiddenUris = getStringListFromJsonArray(params.getJSONArray("forbiddenUris"));
            }
            if (params.has("firstSectionModes")) {
                journeyParameters.firstSectionModes = getStringListFromJsonArray(params.getJSONArray("firstSectionModes"));
            }
            if (params.has("lastSectionModes")) {
                journeyParameters.lastSectionModes = getStringListFromJsonArray(params.getJSONArray("lastSectionModes"));
            }
            if (params.has("count")) {
                journeyParameters.count = params.getInt("count");
            }
            if (params.has("minNbJourneys")) {
                journeyParameters.minNbJourneys = params.getInt("minNbJourneys");
            }
            if (params.has("maxNbJourneys")) {
                journeyParameters.maxNbJourneys = params.getInt("maxNbJourneys");
            }

            intent.putExtra(JourneySolutionsActivity.IntentParameters.journeyParameters.name(), journeyParameters);
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
        DateTime dt = new DateTime();
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            dt = formatter.parseDateTime(value);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dt;
    }

    private List<String> getStringListFromJsonArray(JSONArray array) {
        List<String> list = new ArrayList<String>();
        try {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return list;
    }
}
