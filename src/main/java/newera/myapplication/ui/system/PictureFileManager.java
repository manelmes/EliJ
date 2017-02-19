package newera.myapplication.ui.system;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import newera.myapplication.MainActivity;
import newera.myapplication.image.Image;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Emile Barjou-Suire on 09/02/2017.
 */

public class PictureFileManager {
    public static final int DECODE_TILE_SIZE = 2048;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static int LAST_REQUEST;

    private static MainActivity Activity;

    private static String TmpPicturePath;

    private static File TmpPictureFile;

    private static ParcelFileDescriptor parcelFD;
    private static FileDescriptor fileDescriptor;
    private static Uri TmpUriFile;


    /** Set the reference to MainActivity for systems calls.
     * @param activity the MainActivity.
     */
    static void setActivity(MainActivity activity) {
        Activity = activity;
    }

    /**
     * Calls camera app to take a picture, then saves it in the application directory.
     * Picture can later be retrieved with RetrieveSavedPictureFromIntent().
     */
    public static void CreatePictureFileFromCamera()
    {
        LAST_REQUEST = REQUEST_IMAGE_CAPTURE;
        dispatchTakePictureIntent();
    }

    /**
     * Calls Gallery app to pick an already saved picture from phone.
     * Picture can later be retrieved with RetrieveSavedPictureFromIntent().
     */
    public static void LoadPictureFromGallery()
    {
        dispatchPickPictureFromGallery();
    }

    /**
     * Retrieve a picture previously saved with CreatePictureFileFromCamera() or LoadPictureFromGallery().
     * @return A new Image object of the saved picture
     */
    public static Image RetrieveSavedPictureFromIntent()
    {
        Image result = new Image();
        Bitmap img;

        try {
            if (TmpUriFile != null) {
                parcelFD = Activity.getContentResolver().openFileDescriptor(TmpUriFile, "r");
                fileDescriptor = parcelFD.getFileDescriptor();
                //img = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fileDescriptor, true);
                int h = decoder.getHeight();
                int w = decoder.getWidth();
                int xm, xM, ym, yM;//4160*3120
                Rect r = new Rect();
                result.setDim((int)Math.ceil((double)w/(double)DECODE_TILE_SIZE), (int)Math.ceil((double)h/(double)DECODE_TILE_SIZE));
                for(int y = 0; y < Math.ceil((double)h/(double)DECODE_TILE_SIZE); ++y){
                    for(int x = 0; x < Math.ceil((double)w/(double)DECODE_TILE_SIZE); ++x){
                        xm = x*DECODE_TILE_SIZE;
                        xM = Math.min( (x+1)*DECODE_TILE_SIZE, w );
                        ym = y*DECODE_TILE_SIZE;
                        yM = Math.min( (y+1)*DECODE_TILE_SIZE, h );
                        //Log.i("DBG", "rect= x("+xm +","+xM+"), y("+ym+","+yM+")");
                        r.set(xm, ym, xM, yM);
                        img = decoder.decodeRegion(r, null);
                        result.addBitmap(img, x, y);
                    }
                }
                //result.setBitmap(img);
                parcelFD.close();
            } else {
                Log.i("", "ERROR: TmpUriFile is empty.");
            }
        } catch(IOException e) {
            Log.i("WARNING", "Cannot get file from Uri");
        }

        /*
        switch (LAST_REQUEST)
        {
            case REQUEST_IMAGE_CAPTURE:
                img = BitmapFactory.decodeFile(TmpPicturePath);
                result.setBitmap(img);
                break;

            case REQUEST_IMAGE_GALLERY:
                try {
                    if(TmpUriFile != null) {
                        parcelFD = Activity.getContentResolver().openFileDescriptor(TmpUriFile, "r");
                        fileDescriptor = parcelFD.getFileDescriptor();
                        img = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        result.setBitmap(img);
                        parcelFD.close();
                    } else {
                        Log.i("", "ERROR: TmpUriFile is empty.");
                    }
                } catch (IOException e) {
                    Log.i("WARNING", "Cannot get file from Uri");
                }
                ///*try {
                //img = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                //result.setBitmap(img);
                ///*parcelFD.close();
                //} catch (IOException e){
                //    Log.i("WARNING", "Cannot close parcelFileDescriptor");
                //}

                break;
        }*/

        return result;
    }

    /**
     * Called by SystemActionHandler if an Intent returned something for this class.
     * @param data the data returned from the Intent.
     */
    static void HandleResult(Intent data)
    {
        if(LAST_REQUEST == REQUEST_IMAGE_GALLERY) {
            TmpUriFile = data.getData();
        }

        Activity.civ.setImage(RetrieveSavedPictureFromIntent());
    }

    private static void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(Activity.getPackageManager()) != null)
        {
            try {
                createImageFile();
            }catch (IOException e) {
                Log.i("ERROR", "Failed to create ImageFile");
            }

            if (TmpPictureFile != null)
            {
                Uri pictureFileUri = FileProvider.getUriForFile(Activity, "com.newera.fileprovider", TmpPictureFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri);
                Activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private static void dispatchPickPictureFromGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Activity.startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        LAST_REQUEST = REQUEST_IMAGE_GALLERY;
    }

    private static void createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents

        TmpPicturePath = image.getAbsolutePath();
        TmpPictureFile = image;
        TmpUriFile = Uri.fromFile(TmpPictureFile);

    }
}
