import Foundation
import NavitiaSDKUX

@objc(CDVNavitiaSDKUX) public class CDVNavitiaSDKUX : CDVPlugin {
    @objc(init:)
    public func `init`(command: CDVInvokedUrlCommand) {
        var pluginResult: CDVPluginResult? = nil
        let config: [String, Any?] = command.arguments[0]
        let token: String = config["token"] as? String
        
        if token == nil || token?.count == 0 {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No token provided")
        } else {
            NavitiaSDKUXConfig.setToken
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        }
        
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(invokeJourneyResults:)
    public func invokeJourneyResults(command: CDVInvokedUrlCommand) {
        //let params: [String, Any?] = command.arguments[0]
        let params: JourneySolutionsController.InParameters = JourneySolutionsController.InParameters()
        params.originId = ""
        params.destinationId = ""
        
        let bundle: Bundle? = Bundle(identifier: "org.kisio.NavitiaSDKUX")
        let storyboard: UIStoryboard = UIStoryboard(name: "Journey", bundle: bundle)
        
        let rootViewController: JourneySolutionsController = storyboard.instantiateInitialViewController() as! JourneySolutionsController
        rootViewController.setProps(with: params)
        
        let navigationController: UINavigationController = UINavigationController(rootViewController: rootViewController)
        self.viewController.present(navigationController, animated: true, completion: nil)

        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
    }
}
