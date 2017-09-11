package org.kisio.CDVNavitiaSDKUX;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kisio.NavitiaSDKUX.Config.Configuration;
import org.kisio.NavitiaSDKUX.Controllers.JourneySolutionsActivity;
import org.kisio.NavitiaSDKUX.NavitiaSDKUX;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CDVNavitiaSDKUX extends CordovaPlugin {

    private NavitiaSDKUX navitiaSDKUX;
    private Map<String, Action> actions = new HashMap<String, Action>();

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
        callbackContext.success();
    }

    private void invokeJourneyResults(JSONObject params, CallbackContext callbackContext) {
        final Context context = this.cordova.getActivity().getApplicationContext();
        final Intent intent = new Intent(context, JourneySolutionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final Iterator<String> iterator = params.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                intent.putExtra(key, params.getString(key));
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            }
        }

        context.startActivity(intent);
        callbackContext.success();
    }
}
