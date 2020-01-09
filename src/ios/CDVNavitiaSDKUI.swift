//
//  CDVNavitiaSDKUI.swift
//
//  Copyright © 2018 kisio. All rights reserved.
//

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
        
        let bundle = Bundle(identifier: "org.cocoapods.NavitiaSDKUI") ?? Bundle(identifier: "org.kisio.NavitiaSDKUI")
        let mainColor = toUIColor(hexColor: config["mainColor"] as? String) ?? UIColor(red:0.25, green:0.58, blue:0.56, alpha:1.0)
        let originColor = toUIColor(hexColor: config["originColor"] as? String) ?? UIColor(red:0.00, green:0.73, blue:0.46, alpha:1.0)
        let destinationColor = toUIColor(hexColor: config["destinationColor"] as? String) ?? UIColor(red:0.69, green:0.01, blue:0.33, alpha:1.0)
        let multiNetwork = config["multiNetwork"] as? Bool ?? false
        let formJourney = config["formJourney"] as? Bool ?? false
        let isEarlierLaterFeatureEnabled = config["isEarlierLaterFeatureEnabled"] as? Bool ?? false 
        
        NavitiaSDKUI.shared.initialize(token: token)
        NavitiaSDKUI.shared.bundle = bundle
        NavitiaSDKUI.shared.mainColor = mainColor
        NavitiaSDKUI.shared.originColor = originColor
        NavitiaSDKUI.shared.destinationColor = destinationColor
        NavitiaSDKUI.shared.multiNetwork = multiNetwork
        NavitiaSDKUI.shared.formJourney = formJourney
        NavitiaSDKUI.shared.isEarlierLaterFeatureEnable = isEarlierLaterFeatureEnabled
        
        if let modeForm = config["modeForm"] as? [Any] {
            if let modes = getModes(from: modeForm) {
                NavitiaSDKUI.shared.modeForm = modes
            }
        }
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
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
        
        guard var rootViewController = NavitiaSDKUI.shared.rootViewController else {
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
            self.viewController.navigationController?.pushViewController(rootViewController, animated: true)
        } else {
            let navigationController: UINavigationController = UINavigationController(rootViewController: rootViewController)
            
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
        guard let coverage = arguments["coverage"] as? String else {
            return nil
        }
        
        let journeysRequest = JourneysRequest(coverage: coverage)
        
        journeysRequest.originId = arguments["originId"] as? String
        journeysRequest.destinationId = arguments["destinationId"] as? String
        journeysRequest.originLabel = arguments["originLabel"] as? String
        journeysRequest.destinationLabel = arguments["destinationLabel"] as? String
        journeysRequest.datetimeRepresents = anyToEnum(arguments["datetimeRepresents"]) as CoverageRegionJourneysRequestBuilder.DatetimeRepresents?
        journeysRequest.datetime = getDatetime(from: arguments["datetime"] as? String)
        journeysRequest.forbiddenUris = arguments["forbiddenUris"] as? [String]
        journeysRequest.firstSectionModes = arrayToEnum(arguments["firstSectionModes"]) as [CoverageRegionJourneysRequestBuilder.FirstSectionMode]?
        journeysRequest.lastSectionModes = arrayToEnum(arguments["lastSectionModes"]) as [CoverageRegionJourneysRequestBuilder.LastSectionMode]?
        journeysRequest.count = arguments["count"] as? Int32
        journeysRequest.minNbJourneys = arguments["minNbJourneys"] as? Int32
        journeysRequest.maxNbJourneys = arguments["maxNbJourneys"] as? Int32
        journeysRequest.addPoiInfos = arrayToEnum(arguments["addPoiInfos"]) as [CoverageRegionJourneysRequestBuilder.AddPoiInfos]?
        journeysRequest.directPath = anyToEnum(arguments["directPath"]) as CoverageRegionJourneysRequestBuilder.DirectPath?
        
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
}
