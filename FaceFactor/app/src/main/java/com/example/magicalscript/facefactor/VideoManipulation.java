package com.example.magicalscript.facefactor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class VideoManipulation extends Activity {

    public static final String LOGTAG = "MJPEG_FFMPEG";
    File savePath;
    String _savePath;
    String imagesname;
    String audioFile;
    String outVideoFile;
    ImageView imageView;
    ArrayList<Thread> threads;

    VideoView videoView;
    TextView startAndStopTextView;
    int fromTime;
    int toTime;

    private CascadeClassifier cascadeLEyes;
    private CascadeClassifier cascadeREyes;

    private CascadeClassifier cascadeClassifier;
    private Rect eyearea = new Rect();
    private float _ratioCalc;
    private android.graphics.Point _point1 = new android.graphics.Point();
    private android.graphics.Point _point2 = new android.graphics.Point();


    private void initializeOpenCVDependencies() {
        try {
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
    }

    File files[];
    //Rect[] facesArray ;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private int _pixal = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoshow);
        //textView = (TextView)(findViewById(R.id.textview2));
        initializeOpenCVDependencies();
        imageView = (ImageView) findViewById(R.id.imageView);
        savePath = new File("/data/data/com.example.magicalscript.facefactor/");
        //savePath.mkdirs();
        _savePath = MainActivity._property.get("savepath");
        File videoFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Face Factor Videos/");
        videoFolder.mkdir();

        File f = new File(_savePath);
        DeleteRecursive(f);
        f.mkdir();
        String s = getFileName_CustomFormat();
        imagesname = s + " image-%03d.png";
        MainActivity._property.put("ImagesName", _savePath);
        audioFile = s + " audio.wav";
        MainActivity._property.put("AudioFile", audioFile);
        outVideoFile = s + " Funny Video.mp4";
        MainActivity._property.put("VideoFile", videoFolder.getAbsolutePath() + "/" + outVideoFile);


        //((TextView)(findViewById(R.id.mytextview))).setText(getFPS("2015-10-01 01_07_41.mp4")+ "\n" +"*************"+ "\n" +getInfo() + "\n" + cpuinfo()+ "\n" +getInfoArray().get(cpuInfo.Arm)+ "\n" +getInfoArray().get(cpuInfo.Arm_v7)+ "\n" +getInfoArray().get(cpuInfo.neonSupport));

        String ffmpegPath = "";
        if (getInfoArray().get(cpuInfo.Arm_v7) == "true") {
            ffmpegPath = "armeabi-v7a/ffmpeg";
            if (getInfoArray().get(cpuInfo.neonSupport) == "true")
                ffmpegPath = "armeabi-v7a-neon/ffmpeg";
        } else if (getInfoArray().get(cpuInfo.X86) == "true")
            ffmpegPath = "x86/ffmpeg";

        if (!ffmpegPath.isEmpty()) try {
            InputStream ffmpegInputStream = this.getAssets().open(ffmpegPath);
            FileMover fm = new FileMover(ffmpegInputStream, savePath + "/ffmpeg");
            fm.moveIt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        for (int i = 0; i < libraryAssets.length; i++) {
            try {
                InputStream ffmpegInputStream = this.getAssets().open(libraryAssets[i]);
                FileMover fm = new FileMover(ffmpegInputStream,savePath+ "/" + libraryAssets[i]);
                fm.moveIt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */

        Process process = null;

        try {
            String[] args = {"/system/bin/chmod", "755", savePath + "/ffmpeg"};

            process = new ProcessBuilder(args).start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //**************** step 1 //// Convert to frames
        file = new File(MainActivity._property.get("VideoFileName"));
        threads = new ArrayList<Thread>();

        getRigthResolutin(_pixal);

        // Convert to video
        threads.add(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ProcessVideo(file);
                    }
                })
        );
        //************* step 2
        threads.add(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadImage(MainActivity._property.get("savepath"));
                    }
                })
        );
        //****************** step 3 //convert to audio
        threads.add(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ProcessConvertToAudio(file);
                    }
                })
        );
        //****************** step 4 // rebuilding video code below
        threads.add(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ProcessVideoBuild();
                    }
                })
        );
        // ****************** step 5 // open video player
        // code in muster process
        //

        videoView = (VideoView) this.findViewById(R.id.videoView2);

        //add controls to a MediaPlayer like play, pause.
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        //Set the path of Video or Uri
        videoView.setVideoURI(Uri.parse(MainActivity._property.get("VideoFileName")));
        videoView.start();
        //Set the focus
        videoView.requestFocus();
        startAndStopTextView = (TextView) (findViewById(R.id.textView3));
        fromTime = 0;
        toTime = videoView.getDuration() > 30000 ? 30000 : videoView.getDuration();
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //                      "2015-10-01 01_07_41.mp4"
    File file;
    int looper = 0;

    Bitmap b;

    public void ProcessVideo(File... params) {

        Process ffmpegProcess = null;

        try {

            //ffmpeg -r 10 -b 1800 -i %03d.jpg test1800.mp4
            // 00000
            // /data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg -r p.getPreviewFrameRate() -b 1000 -i frame_%05d.jpg video.mov
            //getApplication().getBaseContext().
            //String[] args2 = {"/data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg", "-y", "-i", "/data/data/com.mobvcasting.ffmpegcommandlinetest/", "-vcodec", "copy", "-acodec", "copy", "-f", "flv", "rtmp://192.168.43.176/live/thestream"};
            //String[] ffmpegCommand = {"/data/data/com.mobvcasting.mjpegffmpeg/ffmpeg", "-r", ""+p.getPreviewFrameRate(), "-b", "1000000", "-vcodec", "mjpeg", "-i", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/frame_%05d.jpg", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/video.mov"};
            String[] Command = {"chmod", "755", savePath + "/ffmpeg"};

            ffmpegProcess = new ProcessBuilder(Command).redirectErrorStream(true).start();
            //ffmpegProcess = Runtime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream _ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader _reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String _line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+" ***Starting FFMPEG***");
            while ((_line = _reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + _line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("***Ending FFMPEG***");

            //ProcessBuilder pb = new ProcessBuilder();
            //Map<String, String> envMap = pb.environment();
            //envMap.put("LD_LIBRARY_PATH", savePath.getAbsolutePath());

            //String[] ffmpegCommand = {savePath + "/ffmpeg", "-i", params[0].getAbsolutePath(), params[0].getParent() + "/image-%03d.jpg"};
            //float _ss = 1/15;   "-vf" , "rotate=-PI/2"   "-metadata:s:v", "rotate=90"
            //if(outHeight<outWidth)

            String _starttime = "00:" + getDate(fromTime, "mm:ss");
            String _duration = "00:" + getDate(toTime - fromTime, "mm:ss");

            String[] _ffmpegCommand = {savePath + "/ffmpeg", "-ss", _starttime, "-i", params[0].getAbsolutePath(), "-t", _duration, "-s", outWidth + "x" + outHeight, "-f", "image2", "-r", "15", _savePath + "/" + imagesname};
            String[] _ffmpegCommand2 = {savePath + "/ffmpeg", "-ss", _starttime, "-i", params[0].getAbsolutePath(), "-vf", "transpose=2", "-t", _duration, "-s", outWidth + "x" + outHeight, "-f", "image2", "-r", "15", _savePath + "/" + imagesname};
            String[] ffmpegCommand = outHeight > outWidth ? _ffmpegCommand2 : _ffmpegCommand;


            ffmpegProcess = new ProcessBuilder(ffmpegCommand).redirectErrorStream(true).start();
            //ffmpegProcess = R untime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+"***Starting FFMPEG***");
            while ((line = reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("\n"+"***Ending FFMPEG***");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ffmpegProcess != null) {
            ffmpegProcess.destroy();
        }


        //in the end we call musterProcess for next statment
        musterProcess();
    }

    private String getFileName_CustomFormat() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    enum cpuInfo {
        abi,
        X86,
        Arm,
        Arm_v7,
        neonSupport,
    }

    private HashMap<cpuInfo, String> getInfoArray() {
        HashMap<cpuInfo, String> sb = new HashMap<cpuInfo, String>();
        sb.put(cpuInfo.abi, Build.CPU_ABI);
        if (Build.CPU_ABI.indexOf("arm") > -1)
            sb.put(cpuInfo.Arm, "true");
        else
            sb.put(cpuInfo.Arm, "false");
        if (Build.CPU_ABI.indexOf("v7") > -1)
            sb.put(cpuInfo.Arm_v7, "true");
        else
            sb.put(cpuInfo.Arm_v7, "false");
        if (Build.CPU_ABI.indexOf("x86") > -1)
            sb.put(cpuInfo.X86, "true");
        else
            sb.put(cpuInfo.X86, "false");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.indexOf("Features") > -1) {
                        if (aLine.indexOf("neon") > -1)
                            sb.put(cpuInfo.neonSupport, "true");
                        else
                            sb.put(cpuInfo.neonSupport, "false");
                    } else if (sb.get(cpuInfo.neonSupport) != "true")
                        sb.put(cpuInfo.neonSupport, "false");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb;
    }
    //Bitmap bitmap;

    Bitmap bMap;
    Bitmap gmap;
    Bitmap cmap;
    int outHeight = 640;
    int outWidth = 480;

    public void loadImage(final String source) {
        try {
            grayscaleImage = new Mat(outHeight, outWidth, CvType.CV_8UC4);
            //grayscaleImage = new Mat(CvType.CV_8UC4);
            // The faces will be a 20% of the height of the screen
            absoluteFaceSize = (int) (outHeight / 4);
            bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
            opt.inDither = false;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            android.graphics.Rect rect = new android.graphics.Rect(-1, -1, -1, -1);
            bitClone = BitmapFactory.decodeStream(getAssets().open("images.png"), rect, opt);
            gmap = BitmapFactory.decodeStream(getAssets().open("glasses.png"), rect, opt);
            cmap = BitmapFactory.decodeStream(getAssets().open("cap.png"), rect, opt);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        files = new File(source).listFiles();

        for (final File f : files) {
            bitmap = faceDrow(BitmapFactory.decodeFile(f.getAbsolutePath()));
            // The faces will be a 20% of the height of the screen
            try {
                absoluteFaceSize = (int) (bitmap.getHeight() * 0.2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SaveImage(bitmap, f.getAbsolutePath());
                }
            }).start();
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ImageView) (findViewById(R.id.imageView2))).setImageBitmap(bitmap);
                    SaveImage(bitmap,source);
                }
            });*/
        }

//in the end we call musterProcess for next statment
        musterProcess();
    }

    int __i = 0;

    public void SaveImage(Bitmap bitmap, String source) {
        /*String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/PhysicsSketchpad";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();*/
        File __file = new File(source);
        try {
            FileOutputStream fOut = new FileOutputStream(__file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fOut);
            fOut.flush();
            fOut.close();
            __i++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Rect[] facesArray;
    //Rect[] eyesArray ;

    Rect eyearea_right;

    public Bitmap faceDrow(Bitmap aInputFrame) {// must resize input image to a small size to improve app speed *********** * * * * *
        try {
            Utils.bitmapToMat(aInputFrame, _aInputFrame);
            Imgproc.cvtColor(_aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
            MatOfRect faces = new MatOfRect();
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }
            facesArray = faces.toArray();

            for (int i = 0; i < facesArray.length; i++)
                try {
                    Rect r = facesArray[i];
                    eyearea = new Rect(r.x + r.width / 8, (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8, (int) (r.height / 3.0));
                    //Imgproc.rectangle(aInputFrame,eyearea.tl(),eyearea.br() , new Scalar(255,0, 0, 255), 2);
                    eyearea_right = new Rect(r.x + r.width / 16, (int) (r.y + (r.height / 4.5)), (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
                    Rect eyearea_left = new Rect(r.x + r.width / 16 + (r.width - 2 * r.width / 16) / 2, (int) (r.y + (r.height / 4.5)), (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

                    detectionEyes(_aInputFrame, cascadeREyes, eyearea_right, 20, _point1);
                    detectionEyes(_aInputFrame, cascadeLEyes, eyearea_left, 20, _point2);
                    //Rect roi = new Rect(new  Point( facesArray[i].x, facesArray[i].y ),new  Size(facesArray[i].width , facesArray[i].height ));
                    //AssetManager am = getAssets();

                    aInputFrame = drowImg(aInputFrame, i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            return aInputFrame;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aInputFrame;
    }

    BitmapFactory.Options opt = new BitmapFactory.Options();
    Mat _aInputFrame = new Mat();

    Bitmap bitmap;
    Bitmap bitClone;
    Bitmap glasseClone;
    Bitmap capClone;
    Canvas canvas = new Canvas();
    Paint paint = new Paint();

    public Bitmap drowImg(Bitmap aInputFrame, int i) {
        try {
            //Mat smallImage = new Mat(facesArray[i].width,facesArray[i].height, aInputFrame.type(),new Scalar(1));//aInputFrame.type());
            bitmap = aInputFrame.copy(Bitmap.Config.ARGB_8888, true);
            bMap = SuperResize(bitClone, facesArray[i].height);
            glasseClone = SuperResizeWidth(gmap, facesArray[i].width);
            capClone = SuperResizeWidth(cmap, facesArray[i].width);
            canvas.setBitmap(bitmap);
            float r = getRotation().floatValue();// *10 just for correct wrone return value
            bMap = RotateBitmap(bMap, r);
            glasseClone = RotateBitmap(glasseClone, r);
            capClone = RotateBitmap(capClone, r);
            canvas.drawBitmap(glasseClone, eyearea_right.x - (eyearea.height / 4), eyearea_right.y - (eyearea.height / 2), paint);
            canvas.drawBitmap(capClone, facesArray[i].x, facesArray[i].y - capClone.getHeight(), paint);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aInputFrame;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void detectionEyes(Mat mRgba, CascadeClassifier clasificator, Rect area, int size, android.graphics.Point p) {
        //Mat template = new Mat();
        Mat mROI = grayscaleImage.submat(area);
        MatOfRect eyes = new MatOfRect();
        //Point iris = new Point();
        //Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());


        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; i++) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            //p point used for calc rotaion face
            p.x = e.x;
            p.y = e.y;
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

    Bitmap __bmap;

    public Double getRotation() {
        float a = _point2.x - _point1.x;
        float b = _point2.y - _point1.y;
        float c = (float) Math.sqrt(
                Math.pow(a, 2) + Math.pow(b, 2)
        );

        double r = Math.toDegrees(Math.acos(a / c));
        if (b < -1)
            r *= -1;
        return r;
    }

    public void getRigthResolutin(int p) {
        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
        mRetriever.setDataSource(file.getAbsolutePath());
        Bitmap frame = mRetriever.getFrameAtTime(5000);

        _pixal = p;
        if (frame.getWidth() > p) {
            outWidth = p;
            float t = (float) outWidth / (float) frame.getWidth();
            int _Height = (int) (t * frame.getHeight());
            outHeight = (2 * ((int) (_Height / 2)) - _Height) < 0 ? _Height + 1 : _Height;
        } else if (frame.getHeight() > p) {
            outHeight = p;
            float t = (float) outHeight / (float) frame.getHeight();
            int _Width = (int) (t * frame.getWidth());
            outWidth = (2 * ((int) (_Width / 2)) - _Width) < 0 ? _Width + 1 : _Width;
        } else {

            outWidth = frame.getWidth();
            outHeight = frame.getHeight();
        }
    }

    public Bitmap SuperResize(Bitmap img, int height) {
        //if (height < img.height())
        //{
        float t = (float) height / (float) img.getHeight(); //height / img.Height. ;
        //System.Math.
        //Size s = new Size((int)(t * img.getWidth()), height);
        __bmap = Bitmap.createScaledBitmap(img, (int) (t * img.getWidth()), height, false);
        //Mat resizedImg = new Mat();
        //Imgproc.resize(img,resizedImg,s);
        return __bmap;
        //}
        //return null;
    }

    public Bitmap SuperResizeWidth(Bitmap img, int Width) {
        //if (height < img.height())
        //{
        float t = (float) Width / (float) img.getWidth(); //height / img.Height. ;
        _ratioCalc = t;
        //System.Math.
        //Size s = new Size((int)(t * img.getWidth()), height);
        Bitmap bmap = Bitmap.createScaledBitmap(img, Width, (int) (t * img.getHeight()), false);
        //Mat resizedImg = new Mat();
        //Imgproc.resize(img,resizedImg,s);
        return bmap;
        //}
        //return null;
    }

    public void ProcessConvertToAudio(File... params) {

        Process ffmpegProcess = null;

        try {

            //ffmpeg -r 10 -b 1800 -i %03d.jpg test1800.mp4
            // 00000
            // /data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg -r p.getPreviewFrameRate() -b 1000 -i frame_%05d.jpg video.mov
            //getApplication().getBaseContext().
            //String[] args2 = {"/data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg", "-y", "-i", "/data/data/com.mobvcasting.ffmpegcommandlinetest/", "-vcodec", "copy", "-acodec", "copy", "-f", "flv", "rtmp://192.168.43.176/live/thestream"};
            //String[] ffmpegCommand = {"/data/data/com.mobvcasting.mjpegffmpeg/ffmpeg", "-r", ""+p.getPreviewFrameRate(), "-b", "1000000", "-vcodec", "mjpeg", "-i", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/frame_%05d.jpg", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/video.mov"};
            String[] Command = {"chmod", "755", savePath + "/ffmpeg"};

            ffmpegProcess = new ProcessBuilder(Command).redirectErrorStream(true).start();
            //ffmpegProcess = Runtime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream _ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader _reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String _line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+"***Starting FFMPEG***");
            while ((_line = _reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + _line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("\n"+"***Ending FFMPEG***");

            //ProcessBuilder pb = new ProcessBuilder();
            //Map<String, String> envMap = pb.environment();
            //envMap.put("LD_LIBRARY_PATH", savePath.getAbsolutePath());

            //String[] ffmpegCommand = {savePath + "/ffmpeg", "-i", params[0].getAbsolutePath(), params[0].getParent() + "/image-%03d.jpg"};
            String _starttime = "00:" + getDate(fromTime, "mm:ss");
            String _duration = "00:" + getDate(toTime - fromTime, "mm:ss");

            String[] ffmpegCommand = {savePath + "/ffmpeg", "-ss", _starttime, "-i", params[0].getAbsolutePath(), "-t", _duration
                    , _savePath + "/" + audioFile};

            ffmpegProcess = new ProcessBuilder(ffmpegCommand).redirectErrorStream(true).start();
            //ffmpegProcess = Runtime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+"***Starting FFMPEG***");
            while ((line = reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("\n"+"***Ending FFMPEG***");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ffmpegProcess != null) {
            ffmpegProcess.destroy();
        }

        //in the end we call musterProcess for next statment
        musterProcess();
    }

    public void ProcessVideoBuild(Void... params) {

        Process ffmpegProcess = null;

        try {

            //ffmpeg -r 10 -b 1800 -i %03d.jpg test1800.mp4
            // 00000
            // /data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg -r p.getPreviewFrameRate() -b 1000 -i frame_%05d.jpg video.mov
            //getApplication().getBaseContext().
            //String[] args2 = {"/data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg", "-y", "-i", "/data/data/com.mobvcasting.ffmpegcommandlinetest/", "-vcodec", "copy", "-acodec", "copy", "-f", "flv", "rtmp://192.168.43.176/live/thestream"};
            //String[] ffmpegCommand = {"/data/data/com.mobvcasting.mjpegffmpeg/ffmpeg", "-r", ""+p.getPreviewFrameRate(), "-b", "1000000", "-vcodec", "mjpeg", "-i", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/frame_%05d.jpg", Environment.getExternalStorageDirectory().getPath() + "/com.mobvcasting.mjpegffmpeg/video.mov"};
            String[] Command = {"chmod", "755", savePath + "/ffmpeg"};

            ffmpegProcess = new ProcessBuilder(Command).redirectErrorStream(true).start();
            //ffmpegProcess = Runtime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream _ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader _reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String _line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+"***Starting FFMPEG***");
            while ((_line = _reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + _line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("\n"+"***Ending FFMPEG***");

            //ProcessBuilder pb = new ProcessBuilder();
            //Map<String, String> envMap = pb.environment();
            //envMap.put("LD_LIBRARY_PATH", savePath.getAbsolutePath());

            //String[] ffmpegCommand = {savePath + "/ffmpeg", "-i", params[0].getAbsolutePath(), params[0].getParent() + "/image-%03d.jpg"};

            String[] ffmpegCommand = {savePath + "/ffmpeg", "-framerate", "15", "-start_number", "001", "-i", _savePath + "/" + imagesname,
                    "-i", _savePath + "/" + audioFile
                    , "-c:v", "libx264", "-c:a", "aac", "-strict", "experimental", "-b:a", "64k", "-pix_fmt", "yuv420p"
                    , MainActivity._property.get("VideoFile")};

            ffmpegProcess = new ProcessBuilder(ffmpegCommand).redirectErrorStream(true).start();
            //ffmpegProcess = Runtime.getRuntime().exec("chmod 700 "+savePath +"/ffmpeg -i "+params[0].getAbsolutePath()+"jj -r 1 "+savePath+"/image-%03d.bmp");

            OutputStream ffmpegOutStream = ffmpegProcess.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));


            String line;

            Log.v(LOGTAG, "***Starting FFMPEG***");
            //textView.append("\n"+"***Starting FFMPEG***");
            while ((line = reader.readLine()) != null) {
                Log.v(LOGTAG, "***" + line + "***");
                //textView.append("\n"+_line);
            }
            Log.v(LOGTAG, "***Ending FFMPEG***");
            //textView.append("\n"+"***Ending FFMPEG***");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ffmpegProcess != null) {
            ffmpegProcess.destroy();
        }

        //in the end we call musterProcess for next statment
        musterProcess();
    }

    int turn = 0;

    public void musterProcess() {

        if (turn < 4) {
            threads.get(turn).start();
            turn++;
        } else {
            final File f = new File(_savePath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeleteRecursive(f);
                }
            }).start();
            Intent intent = new Intent(getBaseContext(), VideoPlayer.class);
            startActivity(intent);
        }
    }

    public void mclkbtn(View v) {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        musterProcess();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        try {
            super.onResume();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clkpixel(View v) {
        Button b = (Button) (v);
        if (_pixal == 720) {
            getRigthResolutin(320);
            b.setText("320 p");
        } else {
            getRigthResolutin(720);
            b.setText("720 p");
        }
    }
/*
    void playRecord(){

        int i = 0;
        String str = (String)spFrequency.getSelectedItem();


        file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int shortSizeInBytes = Short.SIZE/Byte.SIZE;

        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int j = 0;
            while(dataInputStream.available() > 0){
                audioData[j] = dataInputStream.readShort();
                j++;
            }

            dataInputStream.close();
            if (str.equals(getString(R.string.vamp)))
            {
                i = 5000;
            }
            if (str.equals(getString(R.string.slow_motion)))
            {
                i = 6050;
            }
            if (str.equals(getString(R.string.robot)))
            {
                i = 8500;
            }
            if (str.equals(getString(R.string.normal)))
            {
                i = 11025;
            }
            if (str.equals(getString(R.string.chipmunk)))
            {
                i = 16000;
            }
            if (str.equals(getString(R.string.funny)))
            {
                i = 22050;
            }
            if (str.equals(getString(R.string.bee)))
            {
                i = 41000;
            }
            if (str.equals(getString(R.string.elephent)))
            {
                i = 30000;
            }

            audioTrack = new AudioTrack(3,
                    i,
                    2,
                    2,
                    bufferSizeInBytes,
                    1);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
*/

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$//
    String timeLine;

    public void startclk(View v) {
        fromTime = videoView.getCurrentPosition();
        timeLine = "Start : " + getDate(fromTime, "mm:ss");

        if (toTime > fromTime)
            timeLine += "\n   End : " + getDate(toTime, "mm:ss");
        else {
            toTime = fromTime + 15000;
            if (toTime > videoView.getDuration())
                toTime = videoView.getDuration();
            timeLine += "\n   End : " + getDate(toTime, "mm:ss");
        }
        startAndStopTextView.setText(timeLine);
    }

    public void endclk(View v) {
        toTime = videoView.getCurrentPosition();

        if (toTime > fromTime && fromTime - toTime < 30000)
            timeLine = "Start : " + getDate(fromTime, "mm:ss");
        else {
            fromTime = toTime - 15000;
            if (fromTime < 0)
                fromTime = 0;
            timeLine = "Start : " + getDate(fromTime, "mm:ss");
        }
        timeLine += "\n   End : " + getDate(toTime, "mm:ss");
        startAndStopTextView.setText(timeLine);
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
