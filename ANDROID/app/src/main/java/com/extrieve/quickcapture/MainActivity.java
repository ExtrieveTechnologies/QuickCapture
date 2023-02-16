package com.extrieve.quickcapture;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.extrieve.quickcapture.sdk.CameraHelper;
import com.extrieve.quickcapture.sdk.CameraSupport;
import com.extrieve.quickcapture.sdk.ImgException;
import com.extrieve.quickcapture.sdk.ImgHelper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final int RESULT_LOAD_IMAGE = 1003;
    public int REQUEST_CODE_FILE_RETURN = 1002;
    ArrayList<String> FileCollection;
    ImageView selectedImage;
    Button getPictureBtn;
    Button loadFromGalleryBtn;
    Button BuildOutPutBtn;
    View shareImageContainer;
    ImageView shareImageBtn;

    ImgHelper ImageHelper;
    CameraHelper CameraHelper;
    String[] REQUIRED_PERMISSIONS = null;
    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);
        CheckAndPromptPermissions();

        ImageHelper = new ImgHelper(this);
        CameraHelper = new CameraHelper();

        selectedImage = findViewById(R.id.displayImageView);
        getPictureBtn = findViewById(R.id.getPictureButton);
        loadFromGalleryBtn = findViewById(R.id.loadFromGalleryBtn);
        BuildOutPutBtn = findViewById(R.id.buildOutPutBtn);
        shareImageContainer = findViewById(R.id.shareImageButton);
        shareImageBtn = findViewById(R.id.picture);

        BuildOutPutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildTiffFile();
            }
        });

        loadFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.loadImageFromGallery();
            }
        });

        getPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.this.allPermissionsGranted()) {
                    Toast.makeText(MainActivity.this,
                            "Permissions not granted by the user.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    //redirecting to camera
                    OpenCameraActivity();
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    //Log.e(TAG, "photoFile  creation failed", exception);
                    Toast.makeText(MainActivity.this.getActivity(), "Exception Occurred -" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        shareImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dir = FileCollection.get(0);
                MainActivity.this.shareDataExternal(dir);
                Toast.makeText(MainActivity.this.getActivity(), "share", Toast.LENGTH_LONG).show();
            }
        });

        //for setting up default camera configuration
        try {
            SetCameraConfig();
            //UnlockLibrary();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildSDDir() {
        File dir = new File("sdcard/QUICK_CAPTURE");
        try {
            if (dir.mkdir()) {
                return dir.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_RETURN && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Boolean Status = (Boolean) data.getExtras().get("STATUS");
            String Description = (String) data.getExtras().get("DESCRIPTION");
            if (!Status) {
                String imageCaptureLog = "Description : " + Description +
                        ".Exception: " + CameraSupport.CamConfigClass.LastLogInfo;
                Log.d("INFO", imageCaptureLog);
                finishActivity(REQUEST_CODE_FILE_RETURN);
                return;
            }
            FileCollection = (ArrayList<String>) data.getExtras().get("fileCollection");
            if (FileCollection == null || FileCollection.isEmpty()) return;

            try {
                showImages(FileCollection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finishActivity(REQUEST_CODE_FILE_RETURN);
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {

            assert data != null;
            Uri ImUri = data.getData();
            File photoFile;
            String outPath;
            Bitmap photo = null;
            int orientation = 0;
            File imgFile = null;

            try {
                imgFile = new File(BuildStoragePath());
                photoFile = com.extrieve.quickcapture.sdk.CameraHelper.createImageFile(imgFile);
                outPath = photoFile.getAbsolutePath();
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ImUri);

            } catch (IOException ex) {
                Toast.makeText(getActivity(), "photoFile  creation failed", Toast.LENGTH_LONG).show();
                return;
            }
            if (outPath != "") {
                if (photo == null) {
                    Toast.makeText(getActivity(), "Failed to open the photo", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Log.d("AMAL", "CompressToJPEG");
                    ImageHelper.setMaxSize(50);
                    ImageHelper.CompressToJPEG(photo, outPath);
                    Log.d("AMAL", "CompressToJPEG END");
                } catch (ImgException e) {
                    e.printStackTrace();
                    Log.d("AMAL", String.valueOf(e));
                }
                ImageView imageView = findViewById(R.id.displayImageView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(outPath));
                Toast.makeText(this, "Image compressed successfully", Toast.LENGTH_SHORT).show();
                notifyMediaStoreScanner(photoFile);
            }
            finishActivity(RESULT_LOAD_IMAGE);
        }
    }

    private void buildTiffFile() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowProgressToast("building output", true);

            }
        });
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                //To get path from device common storage
                /*Note : All apps that target Android 11 (API level 30) and above are subject to Scoped Storage restrictions
                  and cannot request legacy access to device storage. Instead, they must request a new permission
                  called MANAGE_EXTERNAL_STORAGE (shown to the user as “All Files Access”) to be given broad storage
                  access (excluding a handful of directories like /Android/data or /Android/obb).*/
                //File sdCard = Environment.getExternalStorageDirectory();
                //File dir = new File(sdCard.getAbsolutePath() + "/QuickCapture_Tif/");

                //To get path from app internal storage
                //String path = Environment.getDataDirectory().getAbsolutePath();

                String internalAppPath = BuildStoragePath();
                //File dir = new File (path + "/QuickCapture_Tif/");
                //File dir = new File (sdCard+ "/QuickCapture_Tif/");

                File dir = new File(internalAppPath);

                if (!dir.exists()) dir.mkdirs();
                String strPDFFile = dir + "/PDF_OUTPUT_" + UUID.randomUUID() + ".pdf";
                String strTifFile = dir + "/TIF_OUTPUT_" + UUID.randomUUID() + ".tif";

                String tiffFile = CameraHelper.GetTiffForLastCapture(strTifFile);
                String pdfFile = CameraHelper.GetPDFForLastCapture(strPDFFile);


                if (tiffFile == null || pdfFile == null) {
                    showToast("Output Not Created - Failed", Gravity.CENTER);
                } else {
                    showToast("Success.Output Created", Gravity.CENTER);
                    openPdfFileTrigger(strPDFFile);
                }
                ShowProgressToast("building tiff output", false);

            }

        });
    }

    private void openPdfFileTrigger(String pdfFilePath) {
        File file = new File(pdfFilePath);
        //Uri path = Uri.fromFile(file);
        String PackageName = this.getApplicationContext().getPackageName();
        Uri photoURI = FileProvider.getUriForFile(this, PackageName + ".FileProvider", file);

        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        List<ResolveInfo> resolvedIntentActivities = this.getPackageManager().queryIntentActivities(pdfOpenintent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        pdfOpenintent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            //Log.d(TAG, "Build <= LOLLIPOP");
            pdfOpenintent.setClipData(ClipData.newRawUri("", photoURI));
            pdfOpenintent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        pdfOpenintent.setDataAndType(photoURI, "application/pdf");
        try {
            startActivity(pdfOpenintent);
        } catch (ActivityNotFoundException e) {

        }
    }

    private void writeTiffFileOutput(File tiffFile) throws IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/QuickCapture_Tif/");
        if (!dir.exists()) dir.mkdirs();

        FileOutputStream out = null;
        FileInputStream in = null;
        int cursor;
        try {
            in = new FileInputStream(tiffFile);
            out = new FileOutputStream(dir + "/TIF_OUTPUT.tif");
            while ((cursor = in.read()) != -1) {
                out.write(cursor);
            }
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
            ShowProgressToast("building tiff output", false);
            showToast("Tiff output created : " + dir + "/TIF_OUTPUT.tif", Gravity.CENTER);
        }
    }

    private void showImages(ArrayList<String> FilesPath) throws IOException {
        int FileCollectioLength = FilesPath.size();
        for (int i = 0; i < FileCollectioLength; i++) {
            String dir = FilesPath.get(i);
            File imgFile = new File(dir);
            notifyMediaStoreScanner(imgFile);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = findViewById(R.id.displayImageView);
                myImage.setImageBitmap(myBitmap);
            }
        }
        Toast.makeText(this, "SDK captured " + FileCollectioLength + " images.", Toast.LENGTH_SHORT).show();
    }

    private File getOutputFile() {
        File root = new File(this.getExternalFilesDir(null), "My PDF Folder");

        boolean isFolderCreated = true;

        if (!root.exists()) {
            isFolderCreated = root.mkdir();
        }

        if (isFolderCreated) {
            //  String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "PDF_" + UUID.randomUUID();

            return new File(root, imageFileName + ".pdf");
        } else {
            Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                OpenCameraActivity();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestAllStoragePermission() {
        if (Build.VERSION_CODES.R >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Snackbar.make(findViewById(android.R.id.content), "Permission needed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    //Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                }
                            }
                        })
                        .show();
            }
            if (Environment.isExternalStorageManager()) {
                //todo when permission is granted
                Toast.makeText(getActivity(), "All folder access granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @NotNull
    private String BuildStoragePath() {
        //File direct = new File(Environment.getExternalStorageDirectory(), ".GoNoGoImages");
        //return direct.getAbsolutePath();
        ContextWrapper c = new ContextWrapper(this);
        String path = c.getExternalFilesDir(".QuickCaptureInternals").getAbsolutePath();
        return path;
    }

    private void UnlockLibrary() throws IOException {
        if (!ImgHelper.UnlockImagingLibrary(this, "QuickCaptureSDKLicense.bin")) {
            Toast.makeText(getActivity(), "Library unlock failed + ", Toast.LENGTH_LONG).show();
        }
    }

    private void SetCameraConfig() throws IOException {

        ImageHelper.SetPageLayout(4);//A1-A7(1-7),PHOTO,CUSTOM,ID(8,9,10)

        ImageHelper.SetImageQuality(1);//0,1,2 - Photo_Quality, Document_Quality, Compressed_Document

        ImageHelper.SetDPI(200);//int dpi_val = 100, 150, 200, 300, 500, 600;

        //ImageHelper.setMaxSize(150);

        //can set output file path
        CameraSupport.CamConfigClass.OutputPath = BuildStoragePath();

        // MaxPage = not set / 0 / 1 - single shot mode
        // MaxPage > 1 - Multi capture mode
        // CameraSupport.CamConfigClass.MaxPage = 5;

        CameraSupport.CamConfigClass.MaxPage = 2;

        //Capture sound
        CameraSupport.CamConfigClass.CaptureSound = false;

        CameraSupport.CamConfigClass.EnableFlash = true;

        CameraSupport.CamConfigClass.ShowCaptureCountAndLimit = true;

        CameraSupport.CamConfigClass.UseDefaultCamera = false;

        CameraSupport.CamConfigClass.CameraToggle = 2;
        //0-Disable camera toggle option
        //1-Enable camera toggle option with Front camera by default
        //2-Enable camera toggle option with Back camera by default
    }

    private void OpenCameraActivity() {
        //before starting camera - configuration can set
        String quality = ImageHelper.getCurrentImageQuality();
        try {
            //moving to camera activity in library
            Intent CameraIntent = new Intent(this, Class.forName("com.extrieve.quickcapture.sdk.CameraHelper"));
            //photoURI = CameraSupport.CamConfigClass.OutputPath;
            Uri photoURI = Uri.parse(CameraSupport.CamConfigClass.OutputPath);
            getActivity().grantUriPermission(this.getPackageName(), photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                CameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(CameraIntent, REQUEST_CODE_FILE_RETURN);

        } catch (ClassNotFoundException e) {
            Toast.makeText(getActivity(), "Failed to open camera + ", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void CheckAndPromptPermissions() {
        boolean premissionStatus = false;
        if (allPermissionsGranted()) premissionStatus = true;
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
        for (String permission : REQUIRED_PERMISSIONS) {
            if (permission == "android.permission.MANAGE_EXTERNAL_STORAGE") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //requestAllStoragePermission();
                }
            } else if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public Context getActivity() {
        return this;
    }

    public boolean loadImageFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
        return true;
    }

    private void shareDataExternal(String pathToImage) {

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Send message");
        // (Optional) Here we're passing a content URI to an image to be displayed
        File file = new File(pathToImage);
        if (file.canWrite()) {
            Uri uri = Uri.fromFile(file);
            sendIntent.setData(uri);
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Show the Sharesheet
            startActivity(Intent.createChooser(sendIntent, null));
        } else {
            Toast.makeText(this, "No able to write file - shareDataExternal.", Toast.LENGTH_SHORT).show();
        }
    }

    public final void notifyMediaStoreScanner(final File file) {
        try {
            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            getApplicationContext().sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void ShowProgressToast(String Message, boolean show) {
        try {
            if (pd != null) pd.dismiss();
            if (!show) return;
            pd = new ProgressDialog(MainActivity.this);
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage(Message);
            pd.show();
        } catch (Exception e) {
            Log.d("mainApp", "ShowProgressToast: " + e);
        }
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text, final int TPosition) {
        final Activity activity = MainActivity.this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
                toast.setGravity(TPosition, 0, 10);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 500);
            }
        });
    }
}