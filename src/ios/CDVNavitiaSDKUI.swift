import Foundation
import NavitiaSDKUI

@objc(CDVNavitiaSDKUI) public class CDVNavitiaSDKUI : CDVPlugin {
    @objc(init:)
    public func `init`(command: CDVInvokedUrlCommand) {
        guard let arguments = command.arguments, let config = arguments[0] as? [String: Any] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No valid plugin config")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)

            return
        }

        guard let token: String = config["token"] as? String, !token.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No token provided")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            
            return
        }
        
        var mainColor = "#40958E"
        if let mainColorArg = config["mainColor"] as? String, !mainColorArg.isEmpty {
            mainColor = mainColorArg
        }
        
        var originColor = "#00BB75"
        if let originColorArg = config["originColor"] as? String, !originColorArg.isEmpty {
            originColor = originColorArg
        }
        
        var destinationColor = "#B00353"
        if let destinationColorArg = config["destinationColor"] as? String, !destinationColorArg.isEmpty {
            destinationColor = destinationColorArg
        }
        
        let multiNetwork: Bool = config["multiNetwork"] as? Bool ?? false

        NavitiaSDKUI.shared.initialize(token: token)
        NavitiaSDKUI.shared.mainColor = toUIColor(hexColor: mainColor)
        NavitiaSDKUI.shared.originColor = toUIColor(hexColor: originColor)
        NavitiaSDKUI.shared.destinationColor = toUIColor(hexColor: destinationColor)
        NavitiaSDKUI.shared.multiNetwork = multiNetwork
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(invokeJourneyResults:)
    public func invokeJourneyResults(command: CDVInvokedUrlCommand) {
        guard command.arguments.count > 0, let arguments = command.arguments[0] as? [String: Any] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No parameter specified in invokeJourneyResults")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            
            return
        }
        
        let journeysRequest = self.getJourneysRequest(from: arguments)
        let bundle = Bundle(identifier: "org.cocoapods.NavitiaSDKUI") ?? Bundle(identifier: "org.kisio.NavitiaSDKUI")
        let storyboard = UIStoryboard(name: "Journey", bundle: bundle)
        let listJourneysViewController = storyboard.instantiateInitialViewController() as! ListJourneysViewController
        listJourneysViewController.journeysRequest = journeysRequest
        
        if viewController.navigationController != nil {
            self.viewController.navigationController?.pushViewController(listJourneysViewController, animated: true)
        } else {
            let navigationController: UINavigationController = UINavigationController(rootViewController: listJourneysViewController)
            viewController.present(navigationController, animated: true, completion: nil)
        }
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(resetPreferences:)
    public func resetPreferences(command: CDVInvokedUrlCommand) {
        NavitiaSDKUserDefaultsManager.resetUserDefaults()
    }

    func getJourneysRequest(from arguments: [String: Any]) -> JourneysRequest {
        let originId: String = arguments["originId"] as? String ?? ""
        let destinationId: String = arguments["destinationId"] as? String ?? ""
        var journeysRequest = JourneysRequest(originId: originId, destinationId: destinationId)

        if arguments["originLabel"] != nil {
            journeysRequest.originLabel = arguments["originLabel"] as? String ?? ""
        }
        if arguments["destinationLabel"] != nil {
            journeysRequest.destinationLabel = arguments["destinationLabel"] as? String ?? ""
        }
        if arguments["datetimeRepresents"] != nil {
            if let enumValue = self.anyToEnum(arguments["datetimeRepresents"]!) as JourneysRequestBuilder.DatetimeRepresents? {
                journeysRequest.datetimeRepresents = enumValue
            }
        }
        if arguments["datetime"] != nil {
            journeysRequest.datetime = getDatetime(from: arguments["datetime"] as? String ?? "")
        }
        if arguments["forbiddenUris"] != nil {
            journeysRequest.forbiddenUris = arguments["forbiddenUris"] as? [String] ?? []
        }
        if arguments["firstSectionModes"] != nil {
            journeysRequest.firstSectionModes = self.arrayToEnum(arguments["firstSectionModes"]!) as [JourneysRequestBuilder.FirstSectionMode]
        }
        if arguments["lastSectionModes"] != nil {
            journeysRequest.lastSectionModes = self.arrayToEnum(arguments["lastSectionModes"]!) as [JourneysRequestBuilder.LastSectionMode]
        }
        if arguments["count"] != nil {
            journeysRequest.count = arguments["count"] as? Int32 ?? 0
        }
        if arguments["minNbJourneys"] != nil {
            journeysRequest.minNbJourneys = arguments["minNbJourneys"] as? Int32 ?? 0
        }
        if arguments["maxNbJourneys"] != nil {
            journeysRequest.maxNbJourneys = arguments["maxNbJourneys"] as? Int32 ?? 0
        }
        if arguments["bssStands"] != nil {
            journeysRequest.bssStands = arguments["bssStands"] as? Bool ?? false
        }
        if arguments["addPoiInfos"] != nil {
            journeysRequest.addPoiInfos = self.arrayToEnum(arguments["addPoiInfos"]!) as [JourneysRequestBuilder.AddPoiInfos]
        }
        if arguments["directPath"] != nil, let directPath = self.anyToEnum(arguments["directPath"]!) as JourneysRequestBuilder.DirectPath? {
            journeysRequest.directPath = directPath
        }
        
        return journeysRequest
    }

    func anyToEnum<T: RawRepresentable>(_ value: Any) -> T? {
        return T.init(rawValue: value as! T.RawValue)
    }

    func arrayToEnum<T: RawRepresentable>(_ values: Any) -> [T] {
        let rawValues = values as! [Any]
        var values: [T]  = []
        rawValues.forEach({ rawValue in
            if let value = anyToEnum(rawValue) as T? {
                values.append(value)
            }
        })

        return values
    }

    func getDatetime(from argument: String) -> Date {
        let formatter: DateFormatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        return formatter.date(from: argument)!
    }

    func toUIColor(hexColor: String) -> UIColor {
        var cString:String = hexColor.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        
        if (cString.hasPrefix("#")) {
            cString.remove(at: cString.startIndex)
        }
        
        if ((cString.count) != 6) {
            return UIColor.gray
        }
        
        var rgbValue:UInt32 = 0
        Scanner(string: cString).scanHexInt32(&rgbValue)
        
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
}
