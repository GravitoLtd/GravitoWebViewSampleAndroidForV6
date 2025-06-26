# Gravito WebView-based CMP Integration Guide

## Section 1: General Architecture – WebView-based CMP (Platform-agnostic)

### Overview
Gravito’s WebView-based CMP is a cross-platform solution designed for use in mobile apps. It allows apps to display and interact with the CMP using an embedded web browser (WebView), regardless of the native platform (React Native, Flutter, Native Android, or Native iOS).

### High-Level Flow

1. **CMP HTML Page**
   - Gravito provides an embeddable CMP HTML containing all configuration and JavaScript logic.
   - This page must be hosted by the developer (on a CDN or local server).

2. **WebView Integration**
   - The CMP HTML is loaded into the mobile app’s WebView component.
   - The URL must include `?platform={platformName}` query param (e.g., `reactnative`, `flutter`, `android`, `ios`).
   - This tells the CMP JavaScript how to handle communication for that specific platform.

3. **Communication Mechanism**
   - Communication between the CMP (JavaScript) and the native app occurs through:
     - `window.postMessage` from the CMP
     - Native event listener or handler (e.g., `onMessage`)
     - JavaScript injection (`evaluateJavascript`, `injectJavaScript`, etc.)
   - Based on the platform, different APIs are used to facilitate this message passing.

### Configuration
- In the webview based CMP config make sure you have set below property in config object:
```json
gravito.config.cmp.tcf.core.isWebView = true,
```

#### Showing the CMP UI even if the user has already given consent
- If you want to show the CMP UI even if the user has already given consent, you can set the `gravito.config.cmp.tcf.core.showUiWhenConsented` to `true` in the config object.
```json
gravito.config.cmp.tcf.core.showUiWhenConsented = true,
```

### Core Message Events

| Event Type   | Direction   | Purpose                                                                      |
|--------------|-------------|------------------------------------------------------------------------------|
| CMP-loaded   | CMP → App | CMP is ready and requests consent data                                       |
| cookieData   | App → CMP | App sends existing consent data (if any)                                     |
| save         | CMP → App | User saved consent; app must store this data                                 |
| config       | App → CMP | App configures display properties of CMP UI (optional)                       |
| load         | CMP → App | CMP sends version info (informational)                                       |
| close        | CMP → App | CMP UI closed (informational)                                                |

### App Responsibilities

- Host the CMP HTML provided by Gravito
- Load it in a WebView with the correct platform query param
- Listen to messages from CMP (`CMP-loaded`, `save`)
- Send stored consent data to CMP if available
- Store updated consent data received from CMP
- Optionally configure CMP UI behavior using a `config` message
- Store the tcf consents and related data in a persistent storage solution (e.g., SharedPreferences, UserDefaults, etc.) in the format mentioned in the [TC Data Format](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#how-is-a-cmp-used-in-app).
- if use Google Additional Consent mode you should also store the AcString data in the same persistent storage solution against the key mentioned in the [Google Additional Consent Mode](https://support.google.com/admanager/answer/9681920?hl=en#store-ac-string:~:text=In-,%2D,-app).

Note: All the information about the CMP that needs to be stored in the app is available in the data received in the save event.

### Android Native (Java/Kotlin)

#### Overview

The Android implementation of Gravito CMP uses a WebView that communicates with the native app through a `JavaScriptInterface`. The app handles consent data storage using `SharedPreferences`.

> ⚠️ **Important**: You must register the JavaScript adapter with the exact name used in the CMP JavaScript. For Gravito CMP, the adapter name should be `"AndroidAppWebView"`.

#### Required Setup

- Minimum Android SDK: 21+
- Add Internet permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

#### Required Components

- `WebView` setup in your Activity
- JavaScript interface class (`WebAppInterface`)
- Consent storage using `SharedPreferences`

---

### JavaScript Adapter: `WebAppInterface.java`

```java
package com.example.gravito_android_webview_sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class WebAppInterface {
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public WebAppInterface(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("MYPREF", Context.MODE_PRIVATE);
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void getValueFromWebView(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            String type = jsonObject.optString("type");

            switch (type) {
                case "save":
                    String tcString = jsonObject.optString("tcstring", "");
                    String acString = jsonObject.optString("acstring", "");
                    String nonTcfData = jsonObject.optString("nontcfdata", "");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("TC_STRING", tcString);
                    editor.putString("AC_STRING", acString);
                    editor.putString("NON_TCF_DATA", nonTcfData);
                    editor.apply();
                    break;

                case "close":
                    Log.d("WebAppInterface", "Handling close event");
                    break;

                case "load":
                    Log.d("WebAppInterface", "Handling load event");
                    break;

                default:
                    Log.d("WebAppInterface", "Unknown event type: " + type);
                    break;
            }

        } catch (JSONException e) {
            Log.e("WebAppInterface", "JSON parse error: " + e.getMessage());
        }
    }

    @JavascriptInterface
    public String getValueFromStorage() {
        try {
            String tcString = sharedPreferences.getString("TC_STRING", null);
            String acString = sharedPreferences.getString("AC_STRING", null);
            String nonTcfData = sharedPreferences.getString("NON_TCF_DATA", null);

            JSONObject json = new JSONObject();
            json.put("tcstring", tcString);
            json.put("acstring", acString);
            json.put("nontcfdata", nonTcfData);

            return json.toString();
        } catch (JSONException e) {
            Log.e("WebAppInterface", "JSON creation error: " + e.getMessage());
            return "{}";
        }
    }

    @JavascriptInterface
    public void onButtonClick() {
        // Optional: handle clicks from CMP if required
    }
}
```

---

### MainActivity Setup

```java
package com.example.gravito_android_webview_sample;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView cmpWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cmpWebView = new WebView(this);
        setContentView(cmpWebView);

        WebSettings webSettings = cmpWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Register JavaScript interface with exact name used in CMP HTML
        cmpWebView.addJavascriptInterface(new WebAppInterface(this), "AndroidAppWebView");

        cmpWebView.loadUrl("https://yourdomain.com/gravito-cmp.html?platform=android");
    }

    @Override
    protected void onDestroy() {
        if (cmpWebView != null) {
            cmpWebView.destroy();
        }
        super.onDestroy();
    }
}
```

---

### Opening Preferences UI from Native App

```java
cmpWebView.evaluateJavascript("window.gravito.cmp.openPreferences();", null);
```

---

### Consent Storage

The following keys are used for storing consent data:

| Key             | Value Description                         |
|------------------|--------------------------------------------|
| `TC_STRING`      | TCF v2 consent string (`tcstring`)         |
| `AC_STRING`      | Google Additional Consent string (`acstring`) |
| `NON_TCF_DATA`   | Any non-TCF custom data                    |

Make sure these values are persisted in `SharedPreferences` for future app launches.

---
Note: The `tcstring`, `nontcfdata`, and `acstring` should be stored in the format mentioned in the [TC Data Format](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#how-is-a-cmp-used-in-app).


### Summary