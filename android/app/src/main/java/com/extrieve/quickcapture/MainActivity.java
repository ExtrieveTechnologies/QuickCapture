package com.extrieve.quickcapture;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.extrieve.quickcapture.sdk.CameraHelper;
import com.extrieve.quickcapture.sdk.CameraSupport;
import com.extrieve.quickcapture.sdk.ImgException;
import com.extrieve.quickcapture.sdk.ImgHelper;
import com.extrieve.quickcapture.sdk.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 1001;
    public int REQUEST_CODE_FILE_RETURN = 1002;
    private int RESULT_LOAD_IMAGE = 1003;
    private final String[] REQUIRED_PERMISSIONS =
            new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};
    private boolean premissionStatus = false;
    private boolean _isResultReceived = false;
    private Uri _imageUri = null;
    private static final int REQUEST_CODE_OPEN_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final String QC_FILE_NAME = "photo.jpg";
    private static final String QC_TAG = "QuickCapture";

    ArrayList<String> FileCollection;
    ImageView selectedImage;
    Button getPictureBtn;
    Button loadFromGalleryBtn;
    View shareImageContainer;
    ImageView shareImageBtn;

    ImgHelper ImageHelper;
    CameraHelper CameraHelper;

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
        shareImageContainer = findViewById(R.id.shareImageButton);
        shareImageBtn = findViewById(R.id.picture);

        loadFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.loadImageFromGallery();
                return;
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
          //  UnlockLibrary();
            initPopUPSetUp();
      } catch (IOException e) {
           e.printStackTrace();
        }
        //For test params popup
        //initPopUPSetUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_RETURN && resultCode == Activity.RESULT_OK){
            Boolean Status = (Boolean) data.getExtras().get("STATUS");
            String Description  = (String) data.getExtras().get("DESCRIPTION");
            if(Status == false){
                String imageCaptureLog = "Description : "+ Description +
                        ".Exception: " + CameraSupport.CamConfigClass.LastLogInfo;
                Log.d("INFO", imageCaptureLog);
                finishActivity(REQUEST_CODE_FILE_RETURN);
                return;
            }
            FileCollection = (ArrayList<String>) data.getExtras().get("fileCollection");
            if(FileCollection == null || FileCollection.isEmpty())return;
            try {
                showImages(FileCollection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finishActivity(REQUEST_CODE_FILE_RETURN);
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {

            Uri ImUri= data.getData();
            File photoFile = null;
            String outPath ="";
            Bitmap photo = null;
            ExifInterface exif = null;
            int orientation =0;
            File imgFile = null;

            try {
                imgFile = new  File(BuildStoragePath());
                photoFile =  CameraHelper.createImageFile(imgFile);
                outPath = photoFile.getAbsolutePath();
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(),ImUri);

            } catch (IOException ex) {
                Toast.makeText( getActivity(), "photoFile  creation failed", Toast.LENGTH_LONG ).show();
                return;
            }
            if (outPath!=""){
                if (photo==null){
                    Toast.makeText( getActivity(), "Failed to open the photo", Toast.LENGTH_LONG ).show();
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

    private void showImages( ArrayList<String>  FilesPath) throws IOException {
        int FileCollectioLength = FilesPath.size();
     //   createPDF(FilesPath);
        for (int i = 0; i < FileCollectioLength; i++) {
            String dir = FilesPath.get(i);
            File imgFile = new  File(dir);
            notifyMediaStoreScanner(imgFile);
            if(imgFile.exists()){
                 Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = findViewById(R.id.displayImageView);
                 myImage.setImageBitmap(myBitmap);
                OutputStream outStream = null;
                File filesDir = this.getFilesDir();

             //   File imageFile = new File(filesDir,"CompressionTest.jpg");
               // outStream = new FileOutputStream(imageFile);
              //  myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
              //  outStream.flush();
               // outStream.close();
             //   notifyMediaStoreScanner(imageFile);


                BitmapFactory.Options options = new BitmapFactory.Options();

              //  options.inSampleSize = 4;
               // options.inPurgeable = true;
              //  Bitmap bm = BitmapFactory.decodeFile("your path of image",options);
              //  ByteArrayOutputStream baos = new ByteArrayOutputStream();
              //  Bitmap createBitmap(Bitmap src)
                int bytes = myBitmap.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes);
                myBitmap.copyPixelsToBuffer(buffer);
                // bitmap object
            //    byte[] byteArray = null;
              //  byteArray = buffer.toByteArray();
                byte[] array = buffer.array();

                File file = new File(filesDir,"CompressionTest.jpg");
                try {

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bos.write(array);
                    bos.flush();
                    bos.close();
                    notifyMediaStoreScanner(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //generate base64 string of image
               // String encodedImage =Base64.encodeToString(byteImage_photo,Base64.DEFAULT);

                //send this encoded string to server
//                byte[] byteArray = null;
//                ByteArrayOutputStream stream = new ByteArrayOutputStream ();
//                raw.compress (Bitmap.CompressFormat.JPEG, 100, stream);
//                byteArray = stream.toByteArray ();

//                Bitmap bm = BitmapFactory.decodeFile(imgPathm);
//                ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                byte[] ba = bao.toByteArray();
//                 Glide.with(this)
//                 .load("file://" +imgFile.getAbsolutePath())
//                 .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
//                 .into(myImage);

            }
        }
        // Glide.with(CameraHelper.this)
        // .load("file://" +mFile.getAbsolutePath())
        // .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
        // .into(myImage);
        //   }
    }

    private void createPDF(final ArrayList<String> FileCol) {

      //  File myDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "QuickCaptureFolder");
      //  myDirectory.mkdir();
       // final File file = new File(myDirectory, "AnswerSheet_" + UUID.randomUUID() + ".pdf");
        final File file = getOutputFile();
        final ProgressDialog dialog = ProgressDialog.show(this, "", "Generating PDF...");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap;
                PdfDocument document = new PdfDocument();
                //  int height = 842;
                //int width = 595;
                int height = 1010;
                int width = 714;
                int reqH, reqW;
                reqW = width;

                for (int i = 0; i < FileCol.size(); i++) {
                    //  bitmap = BitmapFactory.decodeFile(array.get(i));
                    bitmap = BitmapFactory.decodeFile(FileCol.get(i));
                    //  bitmap = Utility.getCompressedBitmap(FileCol.get(i), height, width);


                    reqH = bitmap.getHeight();
                    reqW = bitmap.getWidth();
//                    Log.e("reqH", "=" + reqH);
//                    if (reqH < height) {
//                        //  bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
//                    } else {
//                        reqH = height;
//                        reqW = height * bitmap.getWidth() / bitmap.getHeight();
//                        Log.e("reqW", "=" + reqW);
//                        //   bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
//                    }
                    // Compress image by decreasing quality
                    // ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //  bitmap.compress(Bitmap.CompressFormat.WEBP, 50, out);
                    //    bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                    //bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
                    //Create an A4 sized page 595 x 842 in Postscript points.
                    //PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(reqW, reqH, 1).create();
                    PdfDocument.Page page = document.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();

                    Log.e("PDF", "pdf = " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    canvas.drawBitmap(bitmap, 0, 0, null);

                    document.finishPage(page);
                }

                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file);
                    document.writeTo(fos);
                    document.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                        dialog.dismiss();
                        //dismissDialog(dialog);

                    }
                });
            }
        }).start();
    }

    private File getOutputFile(){
        File root = new File(this.getExternalFilesDir(null),"My PDF Folder");

        boolean isFolderCreated = true;

        if (!root.exists()){
            isFolderCreated = root.mkdir();
        }

        if (isFolderCreated) {
          //  String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "PDF_" + UUID.randomUUID();

            return new File(root, imageFileName + ".pdf");
        }
        else {
            Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                OpenCameraActivity();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @NotNull
    private String BuildStoragePath(){
        //File direct = new File(Environment.getExternalStorageDirectory(), ".GoNoGoImages");
        //return direct.getAbsolutePath();
        ContextWrapper c = new ContextWrapper(this);
        String path = c.getExternalFilesDir(".GoNoGoImages").getAbsolutePath();
        return  path;
    }

    private void UnlockLibrary() throws IOException {
        // InputStream QuickCaptureSDKLicense = getAssets().open("QuickCaptureSDKLicense.bin");
        if(!ImageHelper.UnlockImagingLibrary(this,"com.softcell.gonogo.hdbal.bin")){
            Toast.makeText( getActivity(), "Library unlock failed + ", Toast.LENGTH_LONG ).show();
        }
        // CameraSupport.CamConfigClass.LicenseData = QuickCaptureSDKLicense.toString();
    }

    private void SetCameraConfig() throws IOException {

        //double width_val[] = {33.1, 23.4, 16.5, 11.7, 8.3, 5.8, 4.1, 2};
        //double height_val[] = {46.8, 33.1, 24.3, 16.5, 11.7, 8.3, 5.8, 2};

        int Layout = 8;
        int maxsize = 100;
        int DPI = 200;

        ImageHelper.SetPageLayout(4);//A0-A7(0-7),PHOTO,CUSTOM,ID(8,9,10)
       // ImageHelper.SetCustomLayOut(800,500);

        ImageHelper.SetImageQuality(2);//0,1,2 - Photo_Quality, Document_Quality, Compressed_Document

        ImageHelper.SetDPI(DPI);//int dpi_val = 100, 150, 200, 300, 500, 600;

        ImageHelper.setMaxSize(maxsize);

        //can set output file path
        CameraSupport.CamConfigClass.OutputPath = BuildStoragePath();

        // MaxPage = not set / 0 / 1 - single shot mode
        // MaxPage > 1 - Multi capture mode
        CameraSupport.CamConfigClass.MaxPage = 1;

        CameraSupport.CamConfigClass.CaptureReview = true;

        CameraSupport.CamConfigClass.ColorMode = 1; //1-RGB,2-GRAY

        //Capture sound
        CameraSupport.CamConfigClass.CaptureSound = true;

        CameraSupport.CamConfigClass.EnableFlash = true;

        CameraSupport.CamConfigClass.ShowCaptureCountAndLimit = true;

        CameraSupport.CamConfigClass.UseDefaultCamera = false;

//        CameraSupport.CamConfigClass.BottomStampData =
//                "Current Set Image Configuration -> Layout : "+Layout+";Max file size : "+maxsize+" KB;DPI : "+DPI+";"+
//          "\n26/05/2021 20:47:37,Dealer Id - 500222 lat - 17.6784923, lng - 75.914931109999999,12345678"+
//             "\n26/05/2021 20:47:37,Dealer Id - 500222 lat - 17.6784923, lng - 75.914931109999999,12345678";
    }

    private void OpenCameraActivity(){
        //before starting camera - configuration can set
        String quality =  ImageHelper.getCurrentImageQuality();
        try {
            //moving to camera activity in library
           // Intent CameraIntent = new Intent(this,Class.forName("com.extrieve.quickcapture.sdk.CameraHelper"));
            Intent CameraIntent = new Intent(this,Class.forName("com.extrieve.quickcapture.sdk.CameraHelper"));
            //photoURI = CameraSupport.CamConfigClass.OutputPath;
            Uri photoURI = Uri.parse(CameraSupport.CamConfigClass.OutputPath);
            getActivity().grantUriPermission(this.getPackageName(), photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                CameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(CameraIntent,REQUEST_CODE_FILE_RETURN);

        } catch (ClassNotFoundException e) {
            Toast.makeText( getActivity(), "Failed to open camera + ", Toast.LENGTH_LONG ).show();
            e.printStackTrace();
        }
    }

    private void CheckAndPromptPermissions() {
        if (allPermissionsGranted()) premissionStatus = true;
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    public Context getActivity() {
        return this;
    }

    public boolean loadImageFromGallery(){
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
        return true;
    }

    private void shareDataExternal(String pathToImage){

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Send message");
        // (Optional) Here we're passing a content URI to an image to be displayed
        File file = new File(pathToImage);
        if(file.canWrite()) {
            Uri uri = Uri.fromFile(file);
            sendIntent.setData(uri);
            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Show the Sharesheet
            startActivity(Intent.createChooser(sendIntent, null));
        }else {
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


    //....................test params code .............//

    // This button is placed in main activity layout.
    private Button openInputPopupDialogButton = null;
    // This listview is just under above button.
    private ListView userDataListView = null;
    // Below edittext and button are all exist in the popup dialog view.
    private View popupInputDialogView = null;
    // Contains user name data.
    private EditText imgLayoutTxt = null;
    // Contains password data.
    private EditText imgDPITxt = null;
    // Contains email data.
    private EditText imgQualityTxt = null;

    private EditText capture_LimitTxt = null;

    private EditText useDefaultCamTxt = null;
    // Click this button in popup dialog to save user input data in above three edittext.
    private Button saveUserDataButton = null;
    // Click this button to cancel edit user data.
    private Button cancelUserDataButton = null;

    private void initPopUPSetUp(){
        Button button_popup =(Button)findViewById(R.id.button_popup_overlay_input_dialog);
        button_popup.setVisibility(View.VISIBLE);
        initMainActivityControls();
        // When click the open input popup dialog button.
        openInputPopupDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("Enter test values.");
                // alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
                alertDialogBuilder.setCancelable(false);

                // Init popup dialog view and it's ui controls.
                MainActivity.this.initPopupViewControls();

                // Set the inflated layout view object to the AlertDialog builder.
                alertDialogBuilder.setView(popupInputDialogView);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                // When user click the save user data button in the popup dialog.
                saveUserDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view1) {

                        // Get user data from popup dialog editeext.
                        String Img_Layout = imgLayoutTxt.getText().toString();
                        String Img_DPI = imgDPITxt.getText().toString();
                        String Img_Quality = imgQualityTxt.getText().toString();
                        String Capture_Limit = capture_LimitTxt.getText().toString();
                        String useDefaultCam = useDefaultCamTxt.getText().toString();

                        if (Img_Layout == null || Img_Layout.isEmpty() || Img_DPI == null
                                || Img_DPI.isEmpty() || Img_Quality.isEmpty() || Img_Quality == null) {
                            Toast.makeText(MainActivity.this,
                                    "Enter all values correctly",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //double width_val[] = {33.1, 23.4, 16.5, 11.7, 8.3, 5.8, 4.1, 2};
                        //double height_val[] = {46.8, 33.1, 24.3, 16.5, 11.7, 8.3, 5.8, 2};
                        //ImageHelper.SetCustomLayOut(100,10);
                        if(!useDefaultCam.isEmpty() && useDefaultCam == "1")  CameraSupport.CamConfigClass.UseDefaultCamera = true;
                        else CameraSupport.CamConfigClass.UseDefaultCamera = false;

                        ImageHelper.SetPageLayout(Integer.parseInt(Img_Layout));//A0-A7(0-7),PHOTO,CUSTOM(8,9)

                        //ImageHelper.SetImageQuality(2);//0,1,2 - Photo_Quality, Document_Quality, Compressed_Document

                        ImageHelper.SetDPI(Integer.parseInt(Img_DPI));//int dpi_val = 100, 150, 200, 300, 500, 600;

                        //can set output file path
                        CameraSupport.CamConfigClass.OutputPath = MainActivity.this.BuildStoragePath();

                        // MaxPage = not set / 0 / 1 - single shot mode
                        // MaxPage > 1 - Multi capture mode
                        int capLim = Integer.parseInt(Capture_Limit);
                        if(capLim>0) {
                            CameraSupport.CamConfigClass.MaxPage = capLim;
                        }
                        //Capture sound
                        CameraSupport.CamConfigClass.CaptureSound = true;
                        MainActivity.this.OpenCameraActivity();

                        alertDialog.cancel();
                    }
                });

                cancelUserDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
            }
        });
    }

    /* Initialize main activity ui controls ( button and listview ). */
    private void initMainActivityControls()
    {
        if(openInputPopupDialogButton == null)
        {
            openInputPopupDialogButton = findViewById(R.id.button_popup_overlay_input_dialog);
        }

        if(userDataListView == null)
        {
            userDataListView = findViewById(R.id.listview_user_data);
        }
    }

    /* Initialize popup dialog view and ui controls in the popup dialog. */
    private void initPopupViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);

        // Inflate the popup dialog from a layout xml file.
        popupInputDialogView = layoutInflater.inflate(R.layout.popupinputdialog, null);

        // Get user input edittext and button ui controls in the popup dialog.
        imgLayoutTxt = popupInputDialogView.findViewById(R.id.img_layout);
        imgDPITxt = popupInputDialogView.findViewById(R.id.img_dpi);
        imgQualityTxt = popupInputDialogView.findViewById(R.id.img_quality);
        capture_LimitTxt = popupInputDialogView.findViewById(R.id.capture_Limit);
        useDefaultCamTxt = popupInputDialogView.findViewById(R.id.useDefaultCam);


        saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);

        // Display values from the main activity list view in user input edittext.
        initEditTextUserDataInPopupDialog();
    }


    /* Get current user data from listview and set them in the popup dialog edittext controls. */
    private void initEditTextUserDataInPopupDialog()
    {
        List<String> CapParams = getExistUserDataInListView(userDataListView);

        if(CapParams.size() == 3)
        {
            String Img_Layout = CapParams.get(0);

            String Img_DPI = CapParams.get(1);

            String Img_quality = CapParams.get(2);

            String capture_Limit = CapParams.get(3);

            String useDefaultCam =  CapParams.get(4);

            if(Img_Layout == null || Img_Layout.isEmpty() || Img_DPI == null
                    || Img_DPI.isEmpty() || Img_quality.isEmpty() || Img_quality == null)return;

            imgQualityTxt.setText(Img_quality);
            imgDPITxt.setText(Img_DPI);
            imgLayoutTxt.setText(Img_Layout);
            capture_LimitTxt.setText(capture_Limit);
            if(!useDefaultCam.isEmpty()) useDefaultCamTxt.setText(useDefaultCam);
        }
    }

    /* If user data exist in the listview then retrieve them to a string list. */
    private List<String> getExistUserDataInListView(ListView listView)
    {
        List<String> ret = new ArrayList<String>();

        if(listView != null)
        {
            ListAdapter listAdapter = listView.getAdapter();

            if(listAdapter != null) {

                int itemCount = listAdapter.getCount();

                for (int i = 0; i < itemCount; i++) {
                    Object itemObject = listAdapter.getItem(i);
                    HashMap<String, String> itemMap = (HashMap<String, String>)itemObject;

                    Set<String> keySet = itemMap.keySet();

                    Iterator<String> iterator = keySet.iterator();

                    String key = iterator.next();

                    String value = itemMap.get(key);

                    ret.add(value);
                }
            }
        }

        return ret;
    }

//    public static int getRotationFromMediaStore(Context context, Uri imageUri) {
//        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
//        Cursor cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
//        if (cursor == null) return 0;
//
//        cursor.moveToFirst();
//
//        int orientationColumnIndex = cursor.getColumnIndex(columns[1]);
//        return cursor.getInt(orientationColumnIndex);
//    }
//
//    private static int exifToDegrees(int exifOrientation) {
//        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
//            return 90;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
//            return 180;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            return 270;
//        } else {
//            return 0;
//        }
//    }

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = STORE_DIRECTORY;     // getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        //currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }

//    private File getAlbumDir() {
//        File storageDir = null;
//        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
//            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
//        String path = Environment.getExternalStorageDirectory().toString();
//        storageDir = new File(path, "dcim/quickcapture/");
//        if (!storageDir.isDirectory()) if (!storageDir.mkdirs()) {
//            Log.d("CameraSample", "failed to create directory");
//            return null;
//        }
//        return storageDir;
//    }
//
//    private float getRotation() {
//        try {
//            ExifInterface ei = new ExifInterface(currentPhotoPath);
//            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    return 90f;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    return 180f;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    return 270f;
//                default:
//                    return 0f;
//            }
//        } catch (Exception e) {
//            Log.e("Add Recipe", "getRotation", e);
//            return 0f;
//        }
//    }

}