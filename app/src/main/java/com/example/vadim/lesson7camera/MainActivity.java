package com.example.vadim.lesson7camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

  private Bitmap mImageBitmap;
  private String mCurrentPhotoPath;
  private ImageView mImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
    setContentView(R.layout.activity_main);
    mImageView = findViewById(R.id.imgView);
    this.checkCameraHardware(this);
    this.dispatchTakePictureIntent();
  }

  /** Check if this device has a camera */
  private boolean checkCameraHardware(Context context) {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1 && resultCode == RESULT_OK) {
      try {
          mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
          mImageView.setImageBitmap(mImageBitmap);
//                galleryAddPic();
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
  }

  private void dispatchTakePictureIntent() {
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
      // Create the File where the photo should go
      File photoFile = null;
      try {
          photoFile = createImageFile();
      } catch (IOException ex) {
          // Error occurred while creating the File
          Log.i("", "IOException");
      }
      // Continue only if the File was successfully created
      if (photoFile != null) {
          cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
          startActivityForResult(cameraIntent, 1);
      }
    }
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    if(PackageManager.PERMISSION_GRANTED== ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File storageDir = Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES);
      File image = File.createTempFile(
              imageFileName,  // prefix
              ".jpg",         // suffix
              storageDir      // directory
      );

      // Save a file: path for use with ACTION_VIEW intents
      mCurrentPhotoPath = "file:" + image.getAbsolutePath();
      return image;

    } else {
      requestPermission(this);
    }

    return null;

  }

  private static void requestPermission(final Context context){
    if(ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      // For example if the user has previously denied the permission.

      new AlertDialog.Builder(context)
              .setMessage("Allow storage permission in order to store file")
              .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  ActivityCompat.requestPermissions((Activity) context,
                          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          1);
                }
              }).show();

    }else {
      // permission has not been granted yet. Request it directly.
      ActivityCompat.requestPermissions((Activity)context,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              1);
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this,
              "Permission granted",
              Toast.LENGTH_SHORT).show();
      dispatchTakePictureIntent();

    } else {
      Toast.makeText(this,
              "Permission not granted",
              Toast.LENGTH_SHORT).show();
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }
}
