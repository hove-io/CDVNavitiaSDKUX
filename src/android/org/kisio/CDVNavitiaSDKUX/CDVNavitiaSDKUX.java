package org.kisio.CDVNavitiaSDK;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.kisio.NavitiaSDKUX.NavitiaSDKUX;

public class CDVNavitiaSDK extends CordovaPlugin {

    private NavitiaSDKUX navitiaSDK;

    private interface IAction {
        void doAction(JSONObject params, CallbackContext callbackContext);
    }

    private abstract class Action implements IAction {
        @Override
        public abstract void doAction(JSONObject params, CallbackContext callbackContext);
    }

    public CDVNavitiaSDK() {
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

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (actions.containsKey(action)) {
            actions.get(action).doAction(args.getJSONObject(0), callbackContext);
        } else {
            callbackContext.error("Action " + action + " not found");
        }
        return true;
    }

    private void init(JSONObject config, CallbackContext callbackContext) {
        if (!config.has("token")) {
            callbackContext.error("No token specified");
            return;
        }
        this.navitiaSDK = new NavitiaSDKUX();
    }

    private void invokeJourneyResults(JSONObject params, CallbackContext callbackContext) {
        // here, invoke journey result screen
        // NavitiaSDKUX......
    }
}