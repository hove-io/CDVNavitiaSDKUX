import Foundation
import NavitiaSDKUI

@objc(CDVNavitiaSDKUI) public class CDVNavitiaSDKUI : CDVPlugin {
    @objc(init:)
    public func `init`(command: CDVInvokedUrlCommand) {
        var pluginResult: CDVPluginResult? = nil
        let config: [String: String] = command.arguments![0] as! [String: String]
        let token: String = config["token"] ?? ""
        let mainColor: String = config["mainColor"] != nil && !config["mainColor"]!.isEmpty ? config["mainColor"]! : "#40958E"
        let originColor: String = config["originColor"] != nil && !config["originColor"]!.isEmpty ? config["originColor"]! : "#00BB75"
        let destinationColor: String = config["destinationColor"] != nil && !config["destinationColor"]!.isEmpty ? config["destinationColor"]! : "#B00353"

        if token.isEmpty {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No token provided")
        } else {
            NavitiaSDKUI.shared.initialize(token: token)
            NavitiaSDKUI.shared.mainColor = toUIColor(hexColor: mainColor)
            NavitiaSDKUI.shared.originColor = toUIColor(hexColor: originColor)
            NavitiaSDKUI.shared.destinationColor = toUIColor(hexColor: destinationColor)
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        }

        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(invokeJourneyResults:)
    public func invokeJourneyResults(command: CDVInvokedUrlCommand) {
        var pluginResult: CDVPluginResult? = nil
        if command.arguments.count > 0 {
            let bundle = Bundle(identifier: "org.cocoapods.NavitiaSDKUI") ?? Bundle(identifier: "org.kisio.NavitiaSDKUI")
            let storyboard = UIStoryboard(name: "Journey", bundle: bundle)
            let journeyResultsViewController = storyboard.instantiateInitialViewController() as! JourneySolutionViewController
            journeyResultsViewController.inParameters = self.getJourneyInParameters(from: command.arguments![0] as! [String: Any])

            if self.viewController.navigationController != nil {
                self.viewController.navigationController?.pushViewController(journeyResultsViewController, animated: true)
            } else {
                let navigationController: UINavigationController = UINavigationController(rootViewController: journeyResultsViewController)
                self.viewController.present(navigationController, animated: true, completion: nil)
            }

            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No parameter specified in invokeJourneyResults")
        }

        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(resetPreferences:)
    public func resetPreferences(command: CDVInvokedUrlCommand) {
        NavitiaSDKUserDefaultsManager.resetUserDefaults()
    }

    func getJourneyInParameters(from arguments: [String: Any]) -> JourneySolutionViewController.InParameters {
        let originId: String = arguments["originId"] as? String ?? ""
        let destinationId: String = arguments["destinationId"] as? String ?? ""

        var params: JourneySolutionViewController.InParameters = JourneySolutionViewController.InParameters(originId: originId, destinationId: destinationId)

        if (arguments["originLabel"] != nil) {
            params.originLabel = arguments["originLabel"] as? String ?? ""
        }
        if (arguments["destinationLabel"] != nil) {
            params.destinationLabel = arguments["destinationLabel"] as? String ?? ""
        }
        if (arguments["datetimeRepresents"] != nil) {
            if let enumValue = self.anyToEnum(arguments["datetimeRepresents"]!) as JourneysRequestBuilder.DatetimeRepresents? {
                params.datetimeRepresents = enumValue
            }
        }
        if (arguments["datetime"] != nil) {
            params.datetime = getDatetime(from: arguments["datetime"] as? String ?? "")
        }
        if (arguments["forbiddenUris"] != nil) {
            params.forbiddenUris = arguments["forbiddenUris"] as? [String] ?? []
        }
        if (arguments["firstSectionModes"] != nil) {
            params.firstSectionModes = self.arrayToEnum(arguments["firstSectionModes"]!) as [JourneysRequestBuilder.FirstSectionMode]
        }
        if (arguments["lastSectionModes"] != nil) {
            params.lastSectionModes = self.arrayToEnum(arguments["lastSectionModes"]!) as [JourneysRequestBuilder.LastSectionMode]
        }
        if (arguments["count"] != nil) {
            params.count = arguments["count"] as? Int32 ?? 0
        }
        if (arguments["minNbJourneys"] != nil) {
            params.minNbJourneys = arguments["minNbJourneys"] as? Int32 ?? 0
        }
        if (arguments["maxNbJourneys"] != nil) {
            params.maxNbJourneys = arguments["maxNbJourneys"] as? Int32 ?? 0
        }
        if (arguments["bss_stands"] != nil) {
            params.bssStands = arguments["bss_stands"] as? Bool ?? false
        }

        return params
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
