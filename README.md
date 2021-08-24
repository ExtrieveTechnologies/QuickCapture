# QuickCapture
QuickCapture SDK by Extrieve 

![](RackMultipart20210824-4-15adj27_html_24eea43f94968d2b.gif) ![](RackMultipart20210824-4-15adj27_html_6e892562b49803e3.gif)

![](RackMultipart20210824-4-15adj27_html_9c3842334fab774a.png)

## **Android Compression SDK**

**Date:**  **08**** January **** 202 ****1**

![](RackMultipart20210824-4-15adj27_html_a4869ffef093613d.gif)

**Technical Document**

.

Other products.

![](RackMultipart20210824-4-15adj27_html_3daf8a350347b653.png)

![](RackMultipart20210824-4-15adj27_html_32d5e39fa0e01eb4.png)

![](RackMultipart20210824-4-15adj27_html_305993b92c81f64b.png)

# API and integration Details

This section will explain technical information regarding SDK. Mainly the SDK will expose two classes and two supporting classes.

1. **ImgHelper - Purpose** of this class is to handle all imaging related operations
2. **CameraHelper - Handle** the camera related operations. Basically, an activity.
3. **CameraSupport** - Holds various configurations for camera.
4. **ImgException** - Handle all exceptions on Image related operations on _ImgHelper._

Based on the requirement any one or all classes can be used. And need to import those from the SDK.

import com.extrieve.quickcapture.sdk.ImgHelper;

import com.extrieve.quickcapture.sdk.CameraHelper;

import com.extrieve.quickcapture.sdk.CameraSupport;
 import com.extrieve.quickcapture.sdk.ImgException;

# CameraHelper

This class will be implemented as an activity. This class can be initialized as intent.

Intent CameraIntent = new Intent(this,Class._forName_(&quot;com.extrieve.quickcapture.sdk.CameraHelper&quot;));
UriphotoURI=Uri._parse_(CameraSupport.CamConfigClass._OutputPath_);
getActivity().grantUriPermission(this.getPackageName(), photoURI,
Intent._FLAG\_GRANT\_WRITE\_URI\_PERMISSION_ | Intent._FLAG\_GRANT\_READ\_URI\_PERMISSION_);
 if (Build.VERSION._SDK\_INT_ \&lt;= Build.VERSION\_CODES._LOLLIPOP_) {
 CameraIntent.addFlags(Intent._FLAG\_GRANT\_WRITE\_URI\_PERMISSION_);
}
 startActivityForResult(CameraIntent,REQUEST\_CODE\_FILE\_RETURN);

And result of cameraHelper activity can be collected as:

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
super.onActivityResult(requestCode, resultCode, data);
 if (requestCode == REQUEST\_CODE\_FILE\_RETURN &amp;&amp; resultCode == Activity._RESULT\_OK_){
 Boolean Status = (Boolean) data.getExtras().get(&quot;STATUS&quot;);
String Description = (String) data.getExtras().get(&quot;DESCRIPTION&quot;);
 if(Status == false){ //Failed to capture
finishActivity(REQUEST\_CODE\_FILE\_RETURN);
 return;
}
FileCollection = (ArrayList\&lt;String\&gt;) data.getExtras().get(&quot;fileCollection&quot;);
//FileCollection //: will contains all capture images path as string
 finishActivity(REQUEST\_CODE\_FILE\_RETURN);
}

Camera Helper having a supporting class with static configuration

**CameraSupport.CamConfigClass.**** CamConfigClass :**contains various configurations as follows:

1. **OutputPath** - To set the output directory in which the captured images will be saved. Base app should have rights to write to the provided path.

CameraSupport.CamConfigClass._OutputPath_ = &quot;pass output path as string&quot;;

1. **MaxPage** - To set the number of captures to do on each camera session. And this can also control whether the capture mode is single or multi i.e..,
2. if **MaxPage** \&lt;= 0 / not set: means **unlimited**.If **MaxPage** \&gt;= 1: means **limited**.

CameraSupport.CamConfigClass._MaxPage_ = 0;

_MaxPage_ \&lt;= 0: unlimited || \&gt;=1: limited || = 1 single shot mode

1. **ColorMode -** To Set the capture color mode- supporting color and grayscale.

CameraSupport.CamConfigClass._ColorMode_ = 1; //1-RGB,2-GRAY

1. **CaptureReview -** To Enable review for a capture session.

CameraSupport.CamConfigClass._CaptureReview_ = true/false;
2. **DeviceInfo -** Will share all general information about the device.

CameraSupport.CamConfigClass._DeviceInfo_;

1. **LastLogInfo -** Will share all error details from CameraHelper.Will contains the last caught errors from camera helper section.

CameraSupport.CamConfigClass._LastLogInfo_;

1. **CaptureSound -** To Enable camera capture sound.

CameraSupport.CamConfigClass._CaptureSound_ = true;

1. **UseDefaultCamera** - Can Manually set the native camera for any camera2 API failed devices

CameraSupport.CamConfigClass._UseDefaultCamera_ = true;

1. **ShowCaptureCountAndLimit -** Can enable/disable capture limit toast/message &amp; captured number

CameraSupport.CamConfigClass._ShowCaptureCountAndLimit_ = true;

1. **EnableFlash** - Enable Document capture specific flash control for SDK camera.

CameraSupport.CamConfigClass._EnableFlash_ = true;

_CameraHelper_ class will be further optimized as part of the testing and development. Also there will be additional parameter options as well as license initialization as part of the SDK initialization process.

1. **SDKInfo** - Contains all version related information on SDK.

CameraSupport.CamConfigClass._SDKInfo__;_

# ImgHelper

Following are the options/methods available from _class_ _ **ImgHelper** _

ImgHelper ImageHelper = new ImgHelper(this);

1. **SetImageQuality** (ImageQuality value) - Set the Quality of the image, default Document\_Quality is used. If documents are used further for any automations and OCR, use Document\_Quality .

Image Quality values are:

enum ImageQuality {

Photo\_Quality,

Document\_Quality,

Compressed\_Document

}

_Parameters:_ ImageQuality | Return: void

1. **SetPageLayout** (LayoutType ltype)

Set the Layout for the image default layouts are:

enum LayoutType {

A1,

A2,

A3,

A4,

A5,

A6,

A7,

PHOTO,

CUSTOM

}

_Parameters:_ LayoutType | Return: void

1. **SetDPI** (DPI dpi\_value)

Set DPI (depth per inch) for the image.

Default DPI values are:

enum DPI {

DPI\_100,

DPI\_150,

DPI\_200,

DPI\_300,

DPI\_500,

DPI\_600

}

Parameters: DPI | Return: void

1. **SetCustomWidth** (int value)

If image need to set custom width.

Parameters: width value in pixels as integer type | Return: void

1. **SetCustomHeight** (int value)

If image need to set custom height.

Parameters: width value in pixels as integer type | Return: void

1. **SetMaxSize** (long SizeinKB)

Set max size limit for the image. Size will be considered in KB (Kilo Bytes).

Parameters: max size of image in Kilobytes as type long &amp; Return: void

1. Bitmap **GetThumbnail** (Bitmap bm, int reqHeight, int reqWidth,Boolean AspectRatio)

Will build thumbnail for the given image in custom width,height and AspectRatio

Parameters: Bitmap **image** , int **Height** , int **Width** ,Boolean **Aspect**** \_ ****Ratio**

Return: Thumbnail for the image as Bitmap Type.

1. int **GetThumbNailHeight** ()

To get height of the thumbnail to generate

Parameters: N/A | Return: height in pixel as type integer.

1. int **GetThumbNailWidth** ()

To get width of the thumbnail to generate

Parameters: N/A | Return: width in pixel as type integer.

1. Bitmap **LoadImage** (String inputputURI, int reqHeight,int reqWidth)

Will load and build bitmap image from provided URL in requested width and height.

Parameters: String **inputputURI** , int **reqHeight** ,int **reqWidth**

Return: Build image in type Bitmap.

1. Bitmap **rotateBitmap** (Bitmap bitmap, int orientation)

Rotate the image to preferred orientation

Parameters: Bitmap image, int orientation

Return: Rotated image as per given orientation in type Bitmap.

1. Boolean **CompressToJPEG** (Bitmap bm, String outputURI)

Will Compress the provided bitmap image and will save to given path.

Parameters: Bitmap **image** , String **outputURI**

Return: Boolean True/false as per the operation is success or failure

1. Bitmap **CompressAndGetThumbnail** (Bitmap bm, String outputURI,int RotationDegree)

Will compress, rotate and build thumbnail image in bitmap type and will save to given path. And will return the same build thumbnail.

Parameters: Bitmap image, String outputURI,int RotationDegree.

Return: build thumbnail as type bitmap.

14. String **getCurrentImageQuality** ()

Will provide the current set compress quality.

Parameters: Nil.

Return: Current compression quality in string.

**ImgException**  **class**

As a part of exceptional error handling **ImgException** class is available. Following are the possible errors and corresponding codes:

- **CREATE\_FILE\_ERROR = -100;**
- **IMAGE\_ROTATION\_ERROR = -101;**
- **LOAD\_TO\_BUFFER\_ERROR = -102;**
- **DELETE\_FILE\_ERROR = -103;**
- **GET\_ROTATION\_ERROR = -104;**
- **ROTATE\_BITMAP\_ERROR = -105;**
- **BITMAP\_RESIZE\_ERROR = -106;**
- **CAMERA\_HELPER\_ERROR = -107;**
- **LOG\_CREATION\_ERROR = -108;**

**SDK Licensing**

License file provided that should keep inside assets folder of main application and call UnlockImagingLibrary from ImgHelper class to unlock the SDK.

For _UnlockImagingLibrary ,_ need to pass main app _activity context_ and the _file_.

Boolean **UnlockImagingLibrary** (_Context_ **activityContext** , _String_ **licenseFile** )

Will Unlock the Imaging library

Parameters: _Context_ **activityContext** , _String_ **licenseFile**

Return: true/false - unlock success / failed.

ImageHelper._UnlockImagingLibrary_(this,&quot;QuickCaptureSDKLicense.bin&quot;);

Once the license verification is success, then the water mark will disable from the images being generated by the SDK.

10 | Page

[© 1996 - 2021 Extrieve Technologies](https://www.extrieve.com/)
