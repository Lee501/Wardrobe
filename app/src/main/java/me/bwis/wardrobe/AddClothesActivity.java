package me.bwis.wardrobe;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.button.MaterialButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.bwis.wardrobe.utils.RequestCode;

public class AddClothesActivity extends AppCompatActivity {

    private View.OnClickListener mAddPictureButtonOnClickListener;
    private View.OnClickListener mSelectColorButtonOnClickListener;
    private ImageView mClothesImageView;
    private View mColorSelectedView;
    private MaterialButton mSelectColorButton;
    private ColorPicker mColorPicker;

    private int selectedColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothes);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mColorPicker = new ColorPicker(AddClothesActivity.this, 62,39,35);
        mClothesImageView = findViewById(R.id.input_add_picture);
        mColorSelectedView = findViewById(R.id.input_color_selected);
        mSelectColorButton = findViewById(R.id.button_choose_color);
        if (initOnClickListeners())
        {
            mClothesImageView.setOnClickListener(mAddPictureButtonOnClickListener);
            mSelectColorButton.setOnClickListener(mSelectColorButtonOnClickListener);
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirm, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                onAddClothesDone();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }





    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard?");
        builder.setMessage("You will lose all progress!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AddClothesActivity.this.finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    private void onAddClothesDone()
    {
        // TODO read attrs and add to sqlite, return

        this.finish();

    }

    private boolean initOnClickListeners()
    {
        // TODO init onClickListeners
        this.mSelectColorButtonOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPicker.show();
                /* On Click listener for the dialog, when the user select the color */
                Button okColor = (Button) mColorPicker.findViewById(R.id.okColorButton);

                okColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /* You can get single channel (value 0-255) */
                        int selectedColor = mColorPicker.getColor();
                        AddClothesActivity.this.selectedColor = selectedColor;
                        AddClothesActivity.this.mColorSelectedView.setBackgroundColor(selectedColor);
                        mColorPicker.dismiss();
                    }
                });
            }
        };

        this.mAddPictureButtonOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder getImageFrom = new AlertDialog.Builder(AddClothesActivity.this);
                getImageFrom.setTitle("Select:");
                final CharSequence[] opsChars = {getResources().getString(R.string.add_choose_camera),
                        getResources().getString(R.string.add_choose_gallery)};
                getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            dispatchTakePictureIntent();
                        }else
                        if(which == 1){
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,
                                    "Choose From Gallery"), RequestCode.REQUEST_GET_PHOTO_FROM_GALLERY);
                        }
                        dialog.dismiss();
                    }
                });






            }
        };

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case RequestCode.REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK){

                    setPictureOnImageView();
                }

                break;
            case RequestCode.REQUEST_GET_PHOTO_FROM_GALLERY:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    mClothesImageView.setImageURI(null);
                    mClothesImageView.setImageURI(selectedImage);
                }
                break;
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "WRDB_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RequestCode.REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void setPictureOnImageView() {
        // Get the dimensions of the View
        int targetW = mClothesImageView.getWidth();
        int targetH = mClothesImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH)*2;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mClothesImageView.setImageBitmap(bitmap);
    }




}