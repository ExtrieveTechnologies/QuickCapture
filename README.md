<p align="left">
  <img src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/QuickCapture.png?raw=true" width="30%" alt="QuickCapture document scanning SDK logo">
</p>

# QuickCapture: Document Scanning SDK for Android, iOS, Flutter, React Native and Web

**QuickCapture** is Extrieve's document scanning and imaging SDK. It turns a phone camera, a browser or a TWAIN scanner into a document capture point: it captures the page, crops and cleans it, compresses it, and outputs JPEG, PDF or TIFF. Capture and processing run on the device, so documents are not sent to a server to be processed.

[Product page](https://www.extrieve.com/platforms/quickcapture/) · [Documentation](https://docs.extrieve.in/) · [Android demo app](https://play.google.com/store/apps/details?id=com.extrieve.demo) · [Discussions](https://github.com/orgs/ExtrieveTechnologies/discussions/categories/technical-support) · [Contact](https://www.extrieve.com/contact/)

## Choose your platform

<table>
<tr>
<td align="center" width="20%">
<img width="35%" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/android_icon.svg" alt="QuickCapture Android document scanning SDK">
<p><b><a href="https://github.com/ExtrieveTechnologies/QuickCapture_Android">Android</a></b></p>
</td>
<td align="center" width="20%">
<img width="35%" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/ios_icon.svg" alt="QuickCapture iOS document scanning SDK">
<p><b><a href="https://github.com/ExtrieveTechnologies/QuickCapture_IOS">iOS</a></b></p>
</td>
<td align="center" width="20%">
<img width="35%" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/react_native_icon.svg" alt="QuickCapture React Native document scanning SDK">
<p><b><a href="https://github.com/ExtrieveTechnologies/QuickCapture_react_native">React Native</a></b></p>
</td>
<td align="center" width="20%">
<img width="35%" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/flutter_icon.svg" alt="QuickCapture Flutter document scanning SDK">
<p><b><a href="https://pub.dev/packages/quickcapture">Flutter</a></b></p>
</td>
<td align="center" width="20%">
<img width="35%" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/web_icon.svg" alt="QuickCapture Web document scanning SDK">
<p><b><a href="https://github.com/ExtrieveTechnologies/QuickCapture_WEB">Web</a></b></p>
</td>
</tr>
</table>

| Platform | Where to start | Languages |
| --- | --- | --- |
| Android | [QuickCapture_Android](https://github.com/ExtrieveTechnologies/QuickCapture_Android) | Java, Kotlin |
| iOS | [QuickCapture_IOS](https://github.com/ExtrieveTechnologies/QuickCapture_IOS) | Swift, Objective-C |
| React Native | [QuickCapture_react_native](https://github.com/ExtrieveTechnologies/QuickCapture_react_native) · [npm package](https://www.npmjs.com/package/@extrieve_technologies/quickcapture_react_native) · [Expo sample](https://github.com/ExtrieveTechnologies/QuickCapture_react_native_expo_sample) | JavaScript, TypeScript |
| Flutter | [quickcapture on pub.dev](https://pub.dev/packages/quickcapture) | Dart |
| Web | [QuickCapture_WEB](https://github.com/ExtrieveTechnologies/QuickCapture_WEB) | JavaScript |
| Server and desktop | [ImageWizHelper](https://github.com/ExtrieveTechnologies/ImageWizHelper) (QuickCapture Native Imaging SDK) | Java, C# |

Package installs:

```bash
# React Native
npm install @extrieve_technologies/quickcapture_react_native

# Flutter
flutter pub add quickcapture
```

For Android, iOS and Web, follow the setup steps in each repository's README.

## What it does

<table width="100%">
<tr>
<td align="center" width="25%">
<img src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/AI_DOC.svg" alt="Document capture and image optimisation">
<p><b>Capture built for documents</b></p>
<p>Document detection, auto-crop and image optimisation at capture time.</p>
</td>
<td align="center" width="25%">
<img src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/IMG_CTRL.svg" alt="Control DPI, layout and output size">
<p><b>Control over output</b></p>
<p>Set DPI, page layout and output size for images, PDF and TIFF.</p>
</td>
<td align="center" width="25%">
<img src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/SECURE.svg" alt="Offline on-device document scanning">
<p><b>Works offline</b></p>
<p>Capture and processing run on the device, with no data sent to servers or third parties.</p>
</td>
<td align="center" width="25%">
<img src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/img/DEV_FR.svg" alt="Ready-to-use UI components for integration">
<p><b>Quick to integrate</b></p>
<p>Ready-to-use capture UI components for each platform.</p>
</td>
</tr>
</table>

- **Document capture:** a camera tuned for documents, with focus handling, shake detection and guidance for framing the page.
- **Image clean-up:** auto-crop, black and white or greyscale conversion, and shadow and noise removal.
- **Compression:** reduces image size by up to 90% while keeping documents readable, so uploads are faster and storage use is lower.
- **Conversion:** builds single-page or multi-page PDF and TIFF output directly, as well as JPEG.
- **QR and barcode:** scanning and generation.
- **Web scanning:** scan from a web page with standard TWAIN scanners, compressing before upload.

## Common uses

- Customer onboarding and KYC document capture in mobile apps
- Loan and account-opening document collection by field and branch teams
- Scanning from the browser into document management and workflow systems
- Reducing image size before upload to cut bandwidth and storage

## Frequently asked questions

**Which platforms does QuickCapture support?**
Android, iOS, React Native, Flutter and Web. A server and desktop imaging SDK for Windows and Linux is available as ImageWizHelper.

**Does QuickCapture work offline?**
Yes. Capture, image processing, compression and conversion run on the device.

**Which output formats are supported?**
JPEG, PDF and TIFF, including multi-page PDF and TIFF.

**Can I control DPI and file size?**
Yes. You can set the DPI, page layout and output size of the generated files.

**How is QuickCapture licensed?**
QuickCapture is a commercial SDK. The mobile app licence covers deployment on unlimited devices for one application. For pricing or an evaluation licence, see the [product page](https://www.extrieve.com/platforms/quickcapture/) or write to [devsupport@extrieve.com](mailto:devsupport@extrieve.com).

## Related SDKs

- [SplicerAI](https://github.com/ExtrieveTechnologies/SplicerAi): KYC and document data extraction, classification and Aadhaar masking
- [ImageWizHelper](https://github.com/ExtrieveTechnologies/ImageWizHelper): server and desktop image compression and conversion
- [SDK demo app](https://github.com/ExtrieveTechnologies/SDK_DEMO_APP): Android app showing the SDK features

## Support

- **Documentation:** [docs.extrieve.in](https://docs.extrieve.in/)
- **Technical questions:** [GitHub Discussions](https://github.com/orgs/ExtrieveTechnologies/discussions/categories/technical-support)
- **Developer support:** [devsupport@extrieve.com](mailto:devsupport@extrieve.com)
- **Sales and evaluation:** [extrieve.com/contact](https://www.extrieve.com/contact/)

---

© 1996–2026 [Extrieve Technologies](https://www.extrieve.com/). All rights reserved.
