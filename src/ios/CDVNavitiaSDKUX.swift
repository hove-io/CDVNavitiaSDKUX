import Foundation
import NavitiaSDKUX

@objc(CDVNavitiaSDKUX) public class CDVNavitiaSDKUX : CDVPlugin {
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
            NavitiaSDKUXConfig.setToken(token: token)
            NavitiaSDKUXConfig.setTertiaryColor(color: getUIColorFromHexadecimal(hex: mainColor))
            NavitiaSDKUXConfig.setOriginColor(color: getUIColorFromHexadecimal(hex: originColor))
            NavitiaSDKUXConfig.setDestinationColor(color: getUIColorFromHexadecimal(hex: destinationColor))
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        }

        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(invokeJourneyResults:)
    public func invokeJourneyResults(command: CDVInvokedUrlCommand) {
        var pluginResult: CDVPluginResult? = nil
        if command.arguments.count > 0 {
            let params: JourneySolutionsController.InParameters = self.getJourneyInParameters(from: command.arguments![0] as! [String: Any])
            let bundle: Bundle? = Bundle(identifier: "org.kisio.NavitiaSDKUX")
            let storyboard: UIStoryboard = UIStoryboard(name: "Journey", bundle: bundle)

            let rootViewController: JourneySolutionsController = storyboard.instantiateInitialViewController() as! JourneySolutionsController
            rootViewController.setProps(with: params)

            let navigationController: UINavigationController = UINavigationController(rootViewController: rootViewController)
            self.viewController.present(navigationController, animated: true, completion: nil)

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

    func getJourneyInParameters(from arguments: [String: Any]) -> JourneySolutionsController.InParameters {
        let originId: String = arguments["originId"] as? String ?? ""
        let destinationId: String = arguments["destinationId"] as? String ?? ""

        var params: JourneySolutionsController.InParameters = JourneySolutionsController.InParameters(originId: originId, destinationId: destinationId)

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
}
