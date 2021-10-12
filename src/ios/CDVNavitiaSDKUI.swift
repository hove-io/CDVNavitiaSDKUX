//
//  CDVNavitiaSDKUI.swift
//
//  Copyright © 2018 kisio. All rights reserved.
//

import Foundation
import NavitiaSDK
import JourneySDK
import DesignEngine
import ToolboxEngine

@objc(CDVNavitiaSDKUI) public class CDVNavitiaSDKUI : CDVPlugin {
    
    @objc(init:)
    public func `init`(command: CDVInvokedUrlCommand) {
        guard let arguments = command.arguments, let config = arguments[0] as? [String: Any] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No valid plugin config")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        
        guard let token = config["token"] as? String, !token.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No token provided")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        guard let coverage: String = config["coverage"] as? String, !coverage.isEmpty else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No coverage provided")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        do {
            let environment = toExpertEnvironment(environment: config["environment"] as? String ?? "PROD")
            let colorsConfiguration = ColorsConfiguration(      primary: config["primaryColor"] as? String,
                                                               origin: config["originColor"] as? String,
                                                               originIcon: config["originIconColor"] as? String,
                                                               originBackground: config["originBackgroundColor"] as? String,
                                                               destination: config["destinationColor"] as? String,
                                                               destinationIcon: config["destinationIconColor"] as? String,
                                                               destinationBackground: config["destinationBackgroundColor"] as? String)
            let formJourney = config["formJourney"] as? Bool ?? false
            let multiNetwork = config["multiNetwork"] as? Bool ?? false
            let isEarlierLaterFeatureEnabled = config["isEarlierLaterFeatureEnabled"] as? Bool ?? false
            let isNextDeparturesFeatureEnabled = config["isNextDeparturesFeatureEnabled"] as? Bool ?? false
            let maxHistory = config["maxHistory"] as? Int ?? 10
            let modeForm = config["modeForm"] as? [Any]
            let disruptionContributor = config["disruptionContributor"] as? String ?? ""
            let modes = getModes(from: modeForm ?? [])
            let withForm = !(modeForm?.isEmpty ?? true)
            var transportConfiguration = TransportConfiguration(linesConfiguration: [],
                                                                categoriesConfiguration: [],
                                                                providersConfiguration: [])
            var journeyConfiguration = try JourneyConfiguration(colorsConfiguration: colorsConfiguration,
                                                                transportConfiguration: transportConfiguration,
                                                                transportConfigurationJson: nil)
                .withNextDeparturesFeature(enabled: isNextDeparturesFeatureEnabled)
                .withEarlierLaterFeature(enabled: isNextDeparturesFeatureEnabled)
                .withMultiNetwork(enabled: multiNetwork)
                .withDisruptionContributor(disruptionContributor)
                .withForm(enabled: withForm)
                .withMaxHistory(maxHistory)
            
            if withForm {
                journeyConfiguration = journeyConfiguration.withFormCustomTransportModes(modes ?? [])
            }
                
            try JourneySdk.shared.initialize(token: token, coverage: coverage, environment: environment, journeyConfiguration: journeyConfiguration)
            
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
        } catch {
            if let error = error as? MissingColorsConfigurationError {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: String(format: "Journey SDK cannot be initialized! %@", error.localizedDescription))
                commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    }
    
    private func getModes(from arguments: [Any]) -> [ModeButtonModel]? {
        var modeButtonModes = [ModeButtonModel]()
        
        for argument in arguments {
            if let argument = argument as? [String: Any] {
                if let mode = getMode(from: argument) {
                    modeButtonModes.append(mode)
                }
            }
        }
        
        if modeButtonModes.count == 0 {
            return nil
        }
        
        return modeButtonModes
    }
    
    private func getMode(from arguments: [String: Any]) -> ModeButtonModel? {
        guard let title = arguments["title"] as? String,
            let icon = arguments["icon"] as? String,
            let selected = arguments["selected"] as? Bool,
            let firstSectionMode = arguments["firstSectionMode"] as? [String],
            let lastSectionMode = arguments["lastSectionMode"] as? [String] else {
                return nil
        }
        
        let  modeButtonModel = ModeButtonModel(title: title,
                                               type: icon,
                                               iconRes: icon,
                                               selected: selected,
                                               firstSectionMode: firstSectionMode,
                                               lastSectionMode: lastSectionMode,
                                               physicalMode: arguments["physicalMode"] as? [String],
                                               realTime: arguments["realTime"] as? Bool ?? false)
        
        return modeButtonModel
    }
    
    @objc(invokeJourneyResults:)
    public func invokeJourneyResults(command: CDVInvokedUrlCommand) {
        guard command.arguments.count > 0, let arguments = command.arguments[0] as? [String: Any] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No parameter specified in invokeJourneyResults")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        
        guard var rootViewController = JourneySdk.shared.rootViewController else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No root view controller available")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }
        
        rootViewController.journeysRequest = getJourneysRequest(from: arguments)
        
        if navigateToJourneyRootViewController(rootViewController: rootViewController) {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
        } else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error in navigate to NavitaSDKUI")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    private func navigateToJourneyRootViewController(rootViewController: JourneyRootViewController) -> Bool {
        guard let rootViewController = rootViewController as? UIViewController else {
            return false
        }
        
        if viewController.navigationController != nil {
            viewController.navigationController?.modalPresentationStyle = .overCurrentContext
            viewController.navigationController?.pushViewController(rootViewController, animated: true)
        } else {
            let navigationController: UINavigationController = UINavigationController(rootViewController: rootViewController)
            navigationController.modalPresentationStyle = .overCurrentContext
            navigationController.navigationBar.isTranslucent = false
            
            viewController.present(navigationController, animated: true, completion: nil)
        }
        
        return true
    }
    
    @objc(resetPreferences:)
    public func resetPreferences(command: CDVInvokedUrlCommand) {
        NavitiaSDKUserDefaultsManager.resetUserDefaults()
    }
    
    private func getJourneysRequest(from arguments: [String: Any]) -> JourneysRequest? {
        let journeysRequest = JourneysRequest()
        journeysRequest.originId = arguments["originId"] as? String
        journeysRequest.destinationId = arguments["destinationId"] as? String
        journeysRequest.originLabel = arguments["originLabel"] as? String
        journeysRequest.destinationLabel = arguments["destinationLabel"] as? String
        journeysRequest.datetimeRepresents = anyToEnum(arguments["datetimeRepresents"]) as DateTimeRepresents? ?? .departure
        journeysRequest.datetime = getDatetime(from: arguments["datetime"] as? String) ?? Date()
        journeysRequest.forbiddenUris = arguments["forbiddenUris"] as? [String]
        journeysRequest.firstSectionModes = arrayToEnum(arguments["firstSectionModes"]) as [FilterMode]?
        journeysRequest.lastSectionModes = arrayToEnum(arguments["lastSectionModes"]) as [FilterMode]?
        journeysRequest.count = arguments["count"] as? Int
        journeysRequest.minNbJourneys = arguments["minNbJourneys"] as? Int
        journeysRequest.maxNbJourneys = arguments["maxNbJourneys"] as? Int
        journeysRequest.addPoiInfos = arrayToEnum(arguments["addPoiInfos"]) as [AddPoiInfos]?
        journeysRequest.directPath = anyToEnum(arguments["directPath"]) as DirectPath?
        
        return journeysRequest
    }
    
    // MARK: - Helpers
    
    private func anyToEnum<T: RawRepresentable>(_ value: Any?) -> T? {
        guard let rawValue = value as? T.RawValue else {
            return nil
        }
        
        return T.init(rawValue: rawValue)
    }
    
    private func arrayToEnum<T: RawRepresentable>(_ values: Any?) -> [T]? {
        guard let rawValues = values as? [Any] else {
            return nil
        }
        
        var values: [T]  = []
        rawValues.forEach({ rawValue in
            if let value = anyToEnum(rawValue) as T? {
                values.append(value)
            }
        })
        
        return values
    }
    
    private func getDatetime(from argument: String?) -> Date? {
        guard let argument = argument else {
            return nil
        }
        
        let formatter: DateFormatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        return formatter.date(from: argument)
    }
    
    private func toUIColor(hexColor: String?) -> UIColor? {
        guard let hexColor = hexColor else {
            return nil
        }
        
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

    private func toExpertEnvironment(environment: String) -> NavitiaEnvironment {
        switch environment {
        case "CUSTOMER":
            return .customer
        case "DEV":
            return .dev
        case "INTERNAL":
            return .internal
        default:
            return .prod
        }
    }
}
