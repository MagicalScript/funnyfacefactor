package com.example.magicalscript.facefactor;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements CvCameraViewListener ,VideoRecordEvent{

    public static HashMap<String,String> _property = new HashMap<String,String>();
    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private CascadeClassifier cascadeLEyes;
    private CascadeClassifier cascadeREyes;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private Rect eyearea = new Rect();
    //private int _absoluteFaceSize;

    private float _ratioCalc;
    private android.graphics.Point _point1 = new android.graphics.Point();
    private android.graphics.Point _point2 = new android.graphics.Point();


    tabfrag fragmenttab;
    BitmapFactory.Options opt = new BitmapFactory.Options();

    @Override
    public void VideoRecorded() {
        Toast.makeText(this,"play record video",Toast.LENGTH_SHORT).show();
        _savePath = new File(Environment.getExternalStorageDirectory().getPath() + "/com.example.magicalscript.videofram/").getAbsolutePath();
        MainActivity._property.put("savepath",_savePath);
        Intent play_intent = new Intent(this,VideoManipulation.class);
        startActivity(play_intent);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    //must be global variable ******************* * * * * *
    private void initializeOpenCVDependencies(){
        try{
            // Copy the resource into a temp file so OpenCV can load it
            AssetManager am = getAssets();
            InputStream is = am.open("haarcascade_frontalface_alt2.xml");
            File cascadeDir = getDir("cascade", Context.MODE_APPEND);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            //------------------------------------------------------------------------------------//
            // Copy the resource into a temp file so OpenCV can load it
            //AssetManager am = getAssets();
            is = am.open("haarcascade_righteye_2splits.xml");
            //cascadeDir = getDir("cascade", Context.MODE_APPEND);
            mCascadeFile = new File(cascadeDir, "haarcascade_righteye_2splits.xml");
            os = new FileOutputStream(mCascadeFile);

            buffer = new byte[4096];
            //int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeREyes = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            //------------------------------------------------------------------------------------//
            // Copy the resource into a temp file so OpenCV can load it
            //AssetManager am = getAssets();
            is = am.open("haarcascade_lefteye_2splits.xml");
            //cascadeDir = getDir("cascade", Context.MODE_APPEND);
            mCascadeFile = new File(cascadeDir, "haarcascade_lefteye_2splits.xml");
            os = new FileOutputStream(mCascadeFile);

            buffer = new byte[4096];
            //int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeLEyes = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

        // And we are ready to go
        openCvCameraView.enableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //openCvCameraView.enableView();
        super.onCreate(null);
        /*Settings.System.putInt(
                this.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION,
                0 //0 means off, 1 means on
        );*/

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //openCvCameraView = new JavaCameraView(this, 1);
        //setContentView(openCvCameraView);
        //openCvCameraView.setCvCameraViewListener(this);
        setContentView(R.layout.activity_main);
        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        openCvCameraView.setCameraIndex(1);
        openCvCameraView.setCvCameraViewListener(this);
        ////////////// part of taphost Code ////////

        fragmenttab = new tabfrag();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragmenttab).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    String _savePath;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.rec:
                //openCvCameraView.disableView();
                Toast.makeText(this,"start recording",Toast.LENGTH_SHORT).show();
                takeAVideo();

                //openCvCameraView.;
                //Intent intent = new Intent(this,VideoWithSurfaceVw.class);
                //startActivity(intent);
                return true;
            case R.id.add://delete this button
                Toast.makeText(this,"add video",Toast.LENGTH_SHORT).show();
                getImageFromGallery();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*public void addMediaFileDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }*/
    private String getFileName_CustomFormat() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    Uri mfile;
    final int CAMERA_REQUEST = 0;
    final int GALLERY_PICTURE = 1;
    public void takeAVideo() {
        //String VideoFileName=Environment.getExternalStorageDirectory().getPath() + getFileName_CustomFormat() + ".mp4";
        //MainActivity._property.put("VideoFileName",VideoFileName);
        mfile = null;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        //mfile=Uri.fromFile( new File(VideoFileName));
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, mfile );
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, CAMERA_REQUEST);
    }
    public void getImageFromGallery() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Video"), GALLERY_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            mfile = data.getData();
            File gf = new File(mfile.getPath());
            if(!gf.exists())
                gf = new File(getPath(mfile));
            _property.put("VideoFileName", gf.getAbsolutePath());
            if (mfile != null) {
                play();
            }
            super.onActivityResult(requestCode, resultCode, data);

        }catch(Exception e){
            e.printStackTrace();}
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }*/

    public void play() {
        Toast.makeText(this,"play record video",Toast.LENGTH_SHORT).show();
        _savePath = new File(Environment.getExternalStorageDirectory().getPath() + "/.Face Factor temp/").getAbsolutePath();
        _property.put("savepath",_savePath);
        Intent play_intent = new Intent(this,VideoManipulation.class);
        startActivity(play_intent);
    }


    Bitmap bMap;
    Bitmap gmap;
    Bitmap cmap;
    @Override
    public void onCameraViewStarted(int width, int height) {
        try{
            grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
            //grayscaleImage = new Mat(CvType.CV_8UC4);
            // The faces will be a 20% of the height of the screen
            absoluteFaceSize = (int) (height/4);
            bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            opt.inDither = false;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            android.graphics.Rect rect = new android.graphics.Rect(-1, -1, -1, -1);
            bitClone = BitmapFactory.decodeStream(getAssets().open("images.png"), rect, opt);
            gmap = BitmapFactory.decodeStream(getAssets().open("glasses.png"), rect, opt);
            cmap = BitmapFactory.decodeStream(getAssets().open("cap.png"), rect, opt);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
        }catch(Exception e){
            e.printStackTrace();}
    }

    @Override
    public void onCameraViewStopped() {

    }

    Rect[] facesArray ;
    //Rect[] eyesArray ;

    Rect eyearea_right;
    @Override
    public Mat onCameraFrame(Mat aInputFrame) {

        try{
            // Create a grayscale image
            //grayscaleImage = SuperResize(aInputFrame,640);
            Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
            MatOfRect faces = new MatOfRect();

            //absoluteFaceSize = (int) (grayscaleImage.height()/4);

            // Use the classifier to detect faces
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize),new Size());
                        //new Size(_absoluteFaceSize,_absoluteFaceSize), new Size(absoluteFaceSize, absoluteFaceSize));
            }
            //Imgproc.equalizeHist(grayscaleImage,grayscaleImage);
            //Imgproc.equalizeHist(grayscaleImage,grayscaleImage);
            // If th ere are any faces found, draw a rectangle around it
            //Rect[] facesArray = faces.toArray();
            facesArray = faces.toArray();

            for (int i = 0; i <facesArray.length; i++)
                //Toast.makeText(this,"hi hello there",Toast.LENGTH_SHORT);
                try{
                    //Imgproc.circle(aInputFrame, new Point(facesArray[i].x,facesArray[i].y),absoluteFaceSize, new Scalar(0, 255, 0, 255));
                    //Imgproc.circle(aInputFrame, new Point(5,5),5, new Scalar(0, 255, 0, 255));
                    //Imgproc.rectangle(aInputFrame,facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

                    Rect r = facesArray[i];
                    //Imgproc.rectangle(grayscaleImage, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
                    //Imgproc.rectangle(aInputFrame, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);

                    eyearea = new Rect(r.x +r.width/8,(int)(r.y + (r.height/4.5)),r.width - 2*r.width/8,(int)( r.height/3.0));
                    //Imgproc.rectangle(aInputFrame,eyearea.tl(),eyearea.br() , new Scalar(255,0, 0, 255), 2);
                    eyearea_right = new Rect(r.x +r.width/16,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
                    Rect eyearea_left = new Rect(r.x +r.width/16 +(r.width - 2*r.width/16)/2,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));

                    detectionEyes(aInputFrame, cascadeLEyes, eyearea_right, 20, _point1);
                    detectionEyes(aInputFrame, cascadeREyes, eyearea_left, 20, _point2);
                    //Rect roi = new Rect(new  Point( facesArray[i].x, facesArray[i].y ),new  Size(facesArray[i].width , facesArray[i].height ));
                    //AssetManager am = getAssets();

                    drowImg(aInputFrame,i);
                }catch(Exception e){
                    e.printStackTrace();}

            return aInputFrame;
        }catch(Exception e){
            e.printStackTrace();}
        return null;
    }
    @Override
    public void onResume() {
        try{
            super.onResume();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        }catch(Exception e){
            e.printStackTrace();}

    }
    public Mat SuperResize(Mat img, int height)
    {
        //if (height < img.height())
        //{
            float t = (float)height / (float)img.height(); //height / img.Height. ;
            _ratioCalc = t;
            //System.Math.
            Size s = new Size((int)(t * img.width()), height);
            Mat resizedImg = new Mat();
            Imgproc.resize(img,resizedImg,s);
            return resizedImg;
        //}
        //return null;
    }
    public Bitmap SuperResize(Bitmap img, int height)
    {
        //if (height < img.height())
        //{
        float t = (float)height / (float)img.getHeight(); //height / img.Height. ;
        _ratioCalc = t;
        //System.Math.
        //Size s = new Size((int)(t * img.getWidth()), height);
        Bitmap bmap = Bitmap.createScaledBitmap(img,(int)(t * img.getWidth()),height,false);
        //Mat resizedImg = new Mat();
        //Imgproc.resize(img,resizedImg,s);
        return bmap;
        //}
        //return null;
    }
    public Bitmap SuperResizeWidth(Bitmap img, int Width)
    {
        //if (height < img.height())
        //{
        float t = (float)Width / (float)img.getWidth(); //height / img.Height. ;
        _ratioCalc = t;
        //System.Math.
        //Size s = new Size((int)(t * img.getWidth()), height);
        Bitmap bmap = Bitmap.createScaledBitmap(img,Width,(int)(t * img.getHeight()),false);
        //Mat resizedImg = new Mat();
        //Imgproc.resize(img,resizedImg,s);
        return bmap;
        //}
        //return null;
    }
    Bitmap bitmap;
    Bitmap bitClone;
    Bitmap glasseClone;
    Bitmap capClone;
    Canvas canvas = new Canvas();
    Paint paint = new Paint();
    public Mat drowImg(Mat aInputFrame,int i) {
        try {
            //Mat smallImage = new Mat(facesArray[i].width,facesArray[i].height, aInputFrame.type(),new Scalar(1));//aInputFrame.type());

            bMap = SuperResize(bitClone,facesArray[i].height);
            glasseClone = SuperResizeWidth(gmap,facesArray[i].width);
            capClone = SuperResizeWidth(cmap,facesArray[i].width);
            //Bitmap bitmap = Bitmap.createBitmap(aInputFrame.width(),aInputFrame.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(aInputFrame,bitmap);
            //Canvas canvas = new Canvas(bitmap);
            canvas.setBitmap(bitmap);
            //Paint paint = new Paint();
            //paint.setAntiAlias(true);
            //paint.setFilterBitmap(true);
            //paint.setDither(true);
            /*
            Canvas _canvas = new Canvas(bMap);
            Paint _paint = new Paint();
            _paint.setAntiAlias(true);
            _paint.setFilterBitmap(true);
            _paint.setDither(true);
            _canvas.rotate(30);
            */
            float r = getRotation().floatValue();// *10 just for correct wrone return value
            bMap = RotateBitmap(bMap,r);
            glasseClone = RotateBitmap(glasseClone,r);
            capClone = RotateBitmap(capClone,r);

            //canvas.drawBitmap(bMap,facesArray[i].x,facesArray[i].y,paint);
            canvas.drawBitmap(glasseClone,eyearea_right.x-(eyearea.height/4),eyearea_right.y-(eyearea.height/2),paint);
            canvas.drawBitmap(capClone,facesArray[i].x,facesArray[i].y-capClone.getHeight(),paint);
            Utils.bitmapToMat(bitmap,aInputFrame);

            /*
            Utils.bitmapToMat(bMap, smallImage);
            smallImage = SuperResize(smallImage, facesArray[i].height);


            int r = facesArray[i].y;
            int re = r + smallImage.rows();
            int c = facesArray[i].x;
            int ce = c + smallImage.cols();
            smallImage.copyTo(aInputFrame.rowRange(r, re).colRange(c, ce));//.rowRange(r,re).colRange(c,ce)
            */
            return aInputFrame;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void detectionEyes(Mat mRgba,CascadeClassifier clasificator, Rect area,int size, android.graphics.Point p){
        //Mat template = new Mat();
        Mat mROI = grayscaleImage.submat(area);
        MatOfRect eyes = new MatOfRect();
        //Point iris = new Point();
        //Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());


        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; i++){
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            //p point used for calc rotaion face
            p.x=e.x;
            p.y=e.y;
/*
            Rect eye_only_rectangle = new Rect((int)e.tl().x,(int)( e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
            mROI = grayscaleImage.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);
            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            //Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int)iris.x-size/2,(int)iris.y-size/2 ,size,size);
            //Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(), new Scalar(255, 0, 0, 255), 2);
            //template = (grayscaleImage.submat(eye_template)).clone();
*/
        }
    }

    public Double getRotation(){
        float a = _point2.x - _point1.x;
        float b = _point2.y - _point1.y;
        float c = (float)Math.sqrt(
                Math.pow(a,2) + Math.pow(b,2)
        );

        double r = Math.toDegrees(Math.acos(a / c));
        if(b<-1)
            r*=-1;
        return r;
    }

    public double[] printPixelARGB(int pixel) {
        double[] d=new double[3];
        d[0] = (pixel >> 24) & 0xff;
        d[1] = (pixel >> 16) & 0xff;
        d[2] = (pixel >> 8) & 0xff;
        d[3] = (pixel) & 0xff;
        return d;
    }
/*
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        int[] mIntArray = new int[mWidth*mHeight];

// Decode Yuv data to integer array
        decodeYUV420SP(mIntArray, data, mWidth, mHeight);

//Initialize the bitmap, with the replaced color
        Bitmap bmp = Bitmap.createBitmap(mIntArray, mWidth, mHeight, Bitmap.Config.ARGB_8888);

    }
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
*/
}