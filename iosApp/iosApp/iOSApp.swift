import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseMessaging

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        // Initialize Firebase only if properly configured (not a placeholder)
        // This allows the app to build and run without Firebase setup
        if isFirebaseConfigured() {
            FirebaseApp.configure()
            print("Firebase initialized successfully")
        } else {
            print("Firebase not configured - using placeholder GoogleService-Info.plist")
            print("   Push notifications will not work until you add a real Firebase configuration")
        }

        // Initialize KMPNotifier for iOS
        // showPushNotification: When false, foreground push notifications won't be shown
        // askNotificationPermissionOnStart: Automatically ask for notification permission
        // notificationSoundName: Custom sound file name (nil for default)
        NotifierManager.shared.initialize(
            configuration: NotificationPlatformConfigurationIos(
                showPushNotification: true,
                askNotificationPermissionOnStart: true,
                notificationSoundName: nil
            )
        )

        return true
    }

    // Check if Firebase is properly configured (not using placeholder values)
    private func isFirebaseConfigured() -> Bool {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let plist = NSDictionary(contentsOfFile: path),
              let projectId = plist["PROJECT_ID"] as? String
        else {
            return false
        }
        // Check if it's still using placeholder values
        return !projectId.contains("YOUR_PROJECT_ID")
    }

    // Required for push notifications - set APNs token for Firebase Messaging
    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Only set APNs token if Firebase is configured
        if FirebaseApp.app() != nil {
            Messaging.messaging().apnsToken = deviceToken
        }
    }

    // Required for receiving push notification payload data
    func application(_ application: UIApplication,
                     didReceiveRemoteNotification userInfo: [AnyHashable: Any]) async -> UIBackgroundFetchResult {
        NotifierManager.shared.onApplicationDidReceiveRemoteNotification(userInfo: userInfo)
        return UIBackgroundFetchResult.newData
    }
}

@main
struct iOSApp: App {

    // Connect AppDelegate to SwiftUI App lifecycle
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
