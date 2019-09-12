OnSpot@ SDK for Android (Version 1.19.13)
========================================

# Information
OnSpot@ SDK for Android is a client to the OnSpota server scanning the device sensors and responding with events about surrounding spots. 
The library is distributed as an Android AAR file with size of ~500 Kb.

NOTICE:
The repository contains a demo app + the SDK module.
if you need only the SDK module it is located in the onspotsdk foler.

# Android SDK requirements

The minimum Android version for the SDK is Android 5.0 Lollipop (API level 21).
Beacon detection will only work on devices which have support for Bluetooth Low Energy (Bluetooth 4.0). We may add support for Android 4.0 on future versions.

OnSpota's SDK uses Google play services.

## Permissions

For WiFi, geo-location and beacon scanning (starting with Android 6.0) you need to add the location permission.

    android.permission.ACCESS_FINE_LOCATION

Importing the library will automatically add all needed permissions to your app if not already used.

    android.permission.RECEIVE_BOOT_COMPLETED
    android.permission.BLUETOOTH
    android.permission.BLUETOOTH_ADMIN
    android.permission.INTERNET
    android.permission.ACCESS_FINE_LOCATION
    android.permission.ACCESS_WIFI_STATE
    android.permission.CHANGE_WIFI_STATE

# Install

## Android Studio

1. Add module to project

    File -> New -> Import module, Select the onspotasdk module. ->finish
    Make sure the module was added (check the settings.gradle and check that it includes 'onspotasdk' )

2. Select module

    File -> Project Structure -> Modules - {Your app module} -> Dependencies -> Add (+) -> Module dependency -> Select onspotsdk -> OK

3. Check your ```targetSdkVersion``` 

    It is recommended that your TargetSDkVersion will be 21 or higher.
    Please notice that on Android 8.0 (Oreo) or higher the SDK may be less accurate when running in the background.

# Using

1. Request your AppID by support@onspota.com

2. App-ID should be set in the code OnspotaApi#start("MyUserId", "MyAppId") , see chp.4 

	However, for FUTURE USE, Please Set your AppID in the AndroidManifest.xml as well:

		<meta-data
			android:name="com.onspota.sdk.ApplicationId"
			android:value="XXXXXXXXXXX" />

3. Request granted permissions for Manifest.permission.ACCESS_FINE_LOCATION

4. Onspota SDK can use a foreground service to improove its perfomance and accuracy (recommended!). the foreground service is not active most of the time. it is recommended to enable the use of the foreground service especially on Android 7+. for enabling the foreground service use the 'enableForegroundServiceInfo' method with the app's name and an icon for the status bar (shown only when the foreground service is active. hidden most of the time) . see the sample code below.


5. Start the SDK by simply creating a SDK object and calling one of its 'start' methods:

public SdkResult start(String userId); // Uses the application id from the app's manifest.
public SdkResult start(String userId, String appId); // Uses the provided application-id.

Application can always stop the SDK by invoking the 'stop' method from any incatce of the OnspotaApi class.


        // Replace 'MyUserId' with the user's user-id allocated by YOUR service.
	// Replace "MyAppId" with the App ID provided to you by onspota.
        // Replace myContext with the current context (use 'this' when starting the sdk from Activity, Service, etc.)
         // See our demo app for more details.
	 
	 OnspotaApi api = new OnspotaApi(myContext);
         OnspotaApi.SdkResult sdkResult = api.start("MyUserId", "MyAppId");
	 
	if (mForefroundServiceEnable == true) {
		// Enabling foreground service
		api.enableForegroundServiceInfo(getString(R.string.app_name), null, R.mipmap.ic_launcher);
	}
	else {
		// Disabling foreground service (default)
		api.enableForegroundServiceInfo(null, null, 0);
	}

        if (sdkResult == OnspotaApi.SdkResult.Ok) {
               // SDK was started.
        }
        else if (sdkResult == OnspotaApi.SdkResult.AndroidVersionNotSupported) {
               Log.w(TAG,"OnSpota SDK doesn't support current android os");
        }
        else if (sdkResult == OnspotaApi.SdkResult.FailedToFind3rdPartyLib) {
              Log.e(TAG,"Critical error: OnSpota SDK Failed to find at least one of its required dependencies.");
        }
        else if (sdkResult == OnspotaApi.SdkResult.Failed) {
              Log.e(TAG,"OnSpota SDK Failed to start.");
        }
		

6. Register listener for search responses by onspotasdk

		@Override
		public void onResume() {
		    super.onResume();
		    registerReceiver(mEventReceiver, new IntentFilter(getString(com.onspota.sdk.R.string.intent_search)));
		}
		
		@Override
		public void onPause() {
		    super.onPause();
		    unregisterReceiver(mEventReceiver);
		}

7. Obtain a SearchResponse object by the received Intent
            
		public void onReceive(Context context, Intent intent) {
				SearchResponse searchResponse = (SearchResponse) intent.getSerializableExtra(getString(com.onspota.sdk.R.string.intent_search_response));

8. Retrieve surrounding spots, places and incoming events by the SearchResponse object

		List<PlacesListItem> places = searchResponse.getPlaces();
		List<SpotSearchResponse> spots = searchResponse.getSpots();
		List<Event> events = searchResponse.getEvents();
		
		
9. Add location context to custom events:

OnSpota SDK supports geo-analytic feature that adds location context to app custom events such as button click, screen view, etc. This features enables app owners to understand location asspects of their users interations (e.g app usage inside and near stores and user home). Using this feature is done by invoking the '#sendEvent' method of OnspotaApi object:
	
	mOnspotaSdk = new OnspotaApi(myContext);
	.
	. do something
	.
	mOnspotaSdk.sendEvent(new OnspotaSdk.appEvent(ID, category, label));
		
ID is the only mandatory value.
*this function is still not released


# Behaviour

The service for objects scanning starts when you call the 'start' method of the SDK.
The service will be automatically restarted when device is rebooted in response of BOOT_COMPLETED intent.
The periods the service performs scans and execute requests to OnSpot server are smartly controlled by server based on multiple parameters including proximity to the surrounding objects, device movement, power status, user behavior and others. 

Application can always stop the SDK by invoking the 'stop' method from any incatnce of the OnspotaApi class.

# REST Responce

OnSpot@ offers the option to send data on REST to a designated server. The server should be identified and confirmed during your account creation process. 

1. Pass/On/Off events

[POST] https://SERVER_URL
```python
{
    "timestamp": "",    # UTC time, ISO 8601 (event detection time)
    "app_id": "",       # string  (your app id with onspot, you may run more then one app with the same REST service)
    "object_type": "",  # string (S - Spot, G - Geofence, P - Place)
    "object_uuid": "",  # string (Onspot object id)
    "object_name": "",  # string (Human Readable object name)
    "user_id": "",      # string  (Your internal user id, in case provided by the app to Onspot@ SDK)
    "event_type": "",   # string, (PASS, ON, OFF)
    "elapsed_time": 0,  # in seconds
}
```

Copyright (c) 2018 OnSpot@ AG. All rights reserved.
