package com.example.magicalscript.facefactor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
/**
 * Created by Abdellah on 29/09/2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

        private static final String TAG = "CameraPreview";

        private Context mContext;
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private List<Camera.Size> mSupportedPreviewSizes;
        private Camera.Size mPreviewSize;

    public CameraPreview(Context context, Camera camera) {
            super(context);
            mContext = context;
            mCamera = camera;

            // supported preview sizes
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            for(Camera.Size str: mSupportedPreviewSizes)
                Log.e(TAG, str.width + "/" + str.height);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // empty. surfaceChanged will take care of stuff
            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non  -existent preview
            }

            // set preview size and make any resize, rotate or reformatting changes here
            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCamera.setParameters(parameters);
                //mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.e(TAG, "surfaceChanged => w=" + w + ", h=" + h);
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non  -existent preview
            }

            // set preview size and make any resize, rotate or reformatting changes here
            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCamera.setParameters(parameters);
                //mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
/*
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }

            float ratio;
            if(mPreviewSize.height >= mPreviewSize.width)
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            else
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

            // One of these methods should be used, second method squishes preview slightly
            setMeasuredDimension(width, (int) (width * ratio));
//        setMeasuredDimension((int) (width * ratio), height);
        }
    */


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = widthMeasureSpec >= 0
                ? resolveSizeAndState(-1, widthMeasureSpec, 0)
                : getDefaultSize(0, widthMeasureSpec);
        int height = heightMeasureSpec >= 0
                ? resolveSizeAndState(-1, heightMeasureSpec, 0)
                : getDefaultSize(0, heightMeasureSpec);
        //setMeasuredDimension(width, height);
        //final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        //final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        //setMeasuredDimension(500, 400);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        setMeasuredDimension(mPreviewSize.width, mPreviewSize.height);
    }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null)
                return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;

                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }

            return optimalSize;
        }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        int[] mIntArray = new int[mPreviewSize.width*mPreviewSize.height];

// Decode Yuv data to integer array
        decodeYUV420SP(mIntArray, data, mPreviewSize.width, mPreviewSize.height);

//Initialize the bitmap, with the replaced color
        Bitmap bmp = Bitmap.createBitmap(mIntArray, mPreviewSize.width, mPreviewSize.height, Bitmap.Config.ARGB_8888);

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
}