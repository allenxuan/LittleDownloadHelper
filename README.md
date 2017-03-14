# LittleDownloadHelper
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/index.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Version](https://img.shields.io/badge/Version-0.1.0-orange.svg)](https://dl.bintray.com/allenxuan/maven/com/github/allenxuan/littledownloadhelper/0.1.0/)

#### LittleDownloadHelper is a simple utility helping construct background download service on Android Platform.

## Screenshot
<a href="" target="_blank">
  <img alt="demo"
       src="/art/littledownloadhelperdemo.gif"
       width="30%">
</a>

## Gradle Dependency
This library is available on JCenter, so you need add this to your project's build.gradle (usually it is already there by default).
```
allprojects {
    repositories {
        jcenter()
    }
}
```
and add this to your module's build.gradle.
```
dependencies {
    compile 'com.github.allenxuan:littledownloadhelper:0.1.0'
}
```

## How To Use
### Get a LittleDownloadHelper instance.
```
LittleDownloadHelper littleDownloadHelper = new LittleDownloadHelper();
```
or
```
LittleDownloadHelper littleDownloadHelper = new LittleDownloadHelper(context);
```

### Initialize background download service to prepare for upcoming download task.
```
littleDownloadHelper.initDownloadService(context);
```
or
```
littleDownloadHelper.initDownloadService();
```

#### Notice 1: a context should be passed to littleDownloadHelper in either constructor or initDownloadService().

### Start a download task.
```
String url = "http://dl.hdslb.com/mobile/latest/iBiliPlayer-bili.apk";
littleDownloadHelper
     .setDownloadUrl(url)
     .useNotificationSmallIcon(R.drawable.ic_notification)
     .useNotificationLargeIcon(R.mipmap.ic_launcher)
     .useNotificationTargetActivity(MainActivity.class)
     .useDownloadProgressHintStyle(DownloadProgressHintStyle.FRACTION_AND_PERCENT_TOGETHER)
     .startDownload();
```
or
```
String url = "http://dl.hdslb.com/mobile/latest/iBiliPlayer-bili.apk";
littleDownloadHelper
     .useNotificationSmallIcon(R.drawable.ic_notification)
     .startDownload(url);
```
#### Notice 2: notification SmallIcon must be set.
#### Notice 3: there are 3 types of DownloadProgressHint: PERCENT_STYLE, FRACTION_STYLE and FRACTION_AND_PERCENT_TOGETHER, default value is  FRACTION_STYLE.
#### Notice 4: download url can be set via setDownloadUrl(url) or startDownload(url)

### Pause the download task.
```
littleDownloadHelper.pauseDownload();
```

### Cancel the download task.
```
littleDownloadHelper.cancelDownload();
```

### Remove the background download service.
if you are sure that background download service is not needed any longer, invoke this function properly.
```
littleDownloadHelper.destroyDownloadService(context);
```

#### Notice 5: if your context host is killed, such as your app is crashed unexpectedly, and you've not invoked destroyDownloadService(context) yet, then background download service continues running. How can you take control of download service again?
you merely need to get a LittleDownloadHelper instance again，
```
LittleDownloadHelper littleDownloadHelper = new LittleDownloadHelper();
```
initialize download service to get communication with it,
```
littleDownloadHelper.initDownloadService(context);
```
now you can control download service again by pauseDownload(), cancelDownload() or destroyDownloadService().

# License
```
Copyright 2017 Xuanyi Huang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
