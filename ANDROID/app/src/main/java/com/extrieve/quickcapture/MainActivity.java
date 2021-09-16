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
                    Log.d("", "CompressToJPEG");
                    ImageHelper.setMaxSize(50);
                    ImageHelper.CompressToJPEG(photo, outPath);
                    Log.d("", "CompressToJPEG END");
                } catch (ImgException e) {
                    e.printStackTrace();
                    Log.d("", String.valueOf(e));
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
                BitmapFactory.Options options = new BitmapFactory.Options();
                int bytes = myBitmap.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes);
                myBitmap.copyPixelsToBuffer(buffer);
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
            }
        }
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
}