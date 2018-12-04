package com.smaaash.ar.videocamera;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.smaaash.ar.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraGrabber;
import ai.deepar.ar.CameraGrabberListener;
import ai.deepar.ar.CameraOrientation;
import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;

public class VideoCameraActivity extends PermissionsActivity implements AREventListener, SurfaceHolder.Callback {


    private final String TAG = VideoCameraActivity.class.getSimpleName();

    private SurfaceView arView;
    private CameraGrabber cameraGrabber;
    private ImageButton screenshotBtn;
    private ImageButton switchCamera;
    private ImageButton nextMask;
    private ImageButton previousMask;

    private RadioButton radioMasks;
    private RadioButton radioEffects;
    private RadioButton radioFilters;

    private final static String SLOT_MASKS = "masks";
    private final static String SLOT_EFFECTS = "effects";
    private final static String SLOT_FILTER = "filters";


    private String currentSlot = SLOT_MASKS;

    private int currentMask=0;
    private int currentEffect=0;
    private int currentFilter=0;

    private int screenOrientation;

    ArrayList<AREffect> masks;
    ArrayList<AREffect> effects;
    ArrayList<AREffect> filters;

    private DeepAR deepAR;

    public MediaPlayer mediaPlayer = new MediaPlayer();
    public AssetFileDescriptor assetFileDescriptor;

    boolean isUpload = false;

    String whichContent = "singham";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenOrientation = getScreenOrientation();

        deepAR = new DeepAR(this);
        switch (screenOrientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                deepAR.setCameraOrientation(CameraOrientation.LANDSCAPE_LEFT);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                deepAR.setCameraOrientation(CameraOrientation.LANDSCAPE_RIGHT);
                break;
            default:
                deepAR.setCameraOrientation(CameraOrientation.PORTRAIT);
                break;
        }
        deepAR.setAntialiasingLevel(4);
        deepAR.initialize(this, this, CameraResolutionPreset.P640x480);
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }
        });
    }


    public void uploadWithTransferUtility(String keyPath,File filePath) {
        // KEY and SECRET are gotten when we create an IAM user above
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJ7XGOZ5VOESDDMZQ", "TDTjbYesVv4B3zacnMpTu4h62EfwFdHxll9ck4HL");
        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(s3Client)
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload("musicly",
                        keyPath,
                        filePath);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());

    }


    @Override
    protected void onStart() {
        super.onStart();
        String cameraPermission = getResources().getString(R.string.camera_permission);
        String externalStoragePermission = getResources().getString(R.string.external_permission);

        checkMultiplePermissions(
                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
                cameraPermission + " " + externalStoragePermission,
                100,
                new PermissionsActivity.MultiplePermissionsCallback() {
                    @Override
                    public void onAllPermissionsGranted() {
                        setContentView(R.layout.activity_video_camera);
                        setupViews();
                    }

                    @Override
                    public void onPermissionsDenied(List<String> deniedPermissions) {
                        Log.d("MainActity", "Permissions Denied!");
                    }
                });
        whichContent = getIntent().getStringExtra("whichContent");

        if(whichContent.equalsIgnoreCase("singham")) {
            setMask(0);
        }else if(whichContent.equalsIgnoreCase("obama")) {
            setMask(13);
        }
        else if(whichContent.equalsIgnoreCase("tiptip")) {
            setMask(1);
        }
        else if(whichContent.equalsIgnoreCase("pdma")) {
            setMask(16);
        }

    }

    public void playAudio(String name){
        try {
            mediaPlayer.reset();

            assetFileDescriptor = getAssets().openFd(name);

            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            // mp.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setMask(final int whichMask){
        Timer cleaner = new Timer();
        cleaner.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gotoNext(whichMask);

                    }
                });

                return;
            }
        }, 500);

    }

    @Override
    protected void onStop() {
        releaseMediaPlayer();

        cameraGrabber.setFrameReceiver(null);
        cameraGrabber.stopPreview();
        cameraGrabber.releaseCamera();
        cameraGrabber = null;
        super.onStop();

    }

    @Override
    protected void onPause() {
        if (recording) {
            deepAR.pauseVideoRecording();
        }
        super.onPause();
        releaseMediaPlayer();

    }

    private void releaseMediaPlayer() {
        if (mediaPlayer!= null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer= null;
        }
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                  //  Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                      //      "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
//                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
//                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deepAR.setAREventListener(null);
        deepAR.release();
        deepAR = null;
    }

    private void setupEffects() {

        masks = new ArrayList<>();
        masks.add(new AREffect("none", AREffect.EffectTypeMask));
        masks.add(new AREffect("aviators", AREffect.EffectTypeMask));
        masks.add(new AREffect("rain", AREffect.EffectTypeAction));
        masks.add(new AREffect("bigmouth", AREffect.EffectTypeMask));
        masks.add(new AREffect("dalmatian", AREffect.EffectTypeMask));
        masks.add(new AREffect("flowers", AREffect.EffectTypeMask));
        masks.add(new AREffect("koala", AREffect.EffectTypeMask));
        masks.add(new AREffect("lion", AREffect.EffectTypeMask));
        masks.add(new AREffect("smallface", AREffect.EffectTypeMask));
        masks.add(new AREffect("teddycigar", AREffect.EffectTypeMask));
        masks.add(new AREffect("kanye", AREffect.EffectTypeMask));
        masks.add(new AREffect("tripleface", AREffect.EffectTypeMask));
        masks.add(new AREffect("sleepingmask", AREffect.EffectTypeMask));
        masks.add(new AREffect("fatify", AREffect.EffectTypeMask));
        masks.add(new AREffect("obama", AREffect.EffectTypeMask));
        masks.add(new AREffect("mudmask", AREffect.EffectTypeMask));
        masks.add(new AREffect("pug", AREffect.EffectTypeMask));
        masks.add(new AREffect("slash", AREffect.EffectTypeMask));
        masks.add(new AREffect("twistedface", AREffect.EffectTypeMask));
        masks.add(new AREffect("grumpycat", AREffect.EffectTypeMask));

        effects = new ArrayList<>();
        effects.add(new AREffect("none", AREffect.EffectTypeAction));
        effects.add(new AREffect("fire", AREffect.EffectTypeAction));
        effects.add(new AREffect("rain", AREffect.EffectTypeAction));
        effects.add(new AREffect("heart", AREffect.EffectTypeAction));
        effects.add(new AREffect("blizzard", AREffect.EffectTypeAction));

        filters = new ArrayList<>();
        filters.add(new AREffect("none", AREffect.EffectTypeFilter));
        filters.add(new AREffect("oilpaint", AREffect.EffectTypeFilter));
        filters.add(new AREffect("filmcolorperfection", AREffect.EffectTypeFilter));
        filters.add(new AREffect("tv80", AREffect.EffectTypeFilter));
        filters.add(new AREffect("drawingmanga", AREffect.EffectTypeFilter));
        filters.add(new AREffect("sepia", AREffect.EffectTypeFilter));
        filters.add(new AREffect("bleachbypass", AREffect.EffectTypeFilter));
        filters.add(new AREffect("sharpen", AREffect.EffectTypeFilter));
        filters.add(new AREffect("realvhs", AREffect.EffectTypeFilter));


    }

    private void radioButtonClicked() {
        if (radioMasks.isChecked()) {
            currentSlot = SLOT_MASKS;
        } else if (radioEffects.isChecked()) {
            currentSlot = SLOT_EFFECTS;
        } else if (radioFilters.isChecked()) {
            currentSlot = SLOT_FILTER;
        }
    }

    private ArrayList<AREffect> getActiveList() {
        if (currentSlot.equals(SLOT_MASKS)) {
            return masks;
        } else if (currentSlot.equals(SLOT_EFFECTS)) {
            return effects;
        } else {
            return filters;
        }
    }

    private int getActiveIndex() {
        if (currentSlot.equals(SLOT_MASKS)) {
            return currentMask;
        } else if (currentSlot.equals(SLOT_EFFECTS)) {
            return currentEffect;
        } else {
            return currentFilter;
        }
    }

    private void setActiveIndex(int index) {
        if (currentSlot.equals(SLOT_MASKS)) {
            currentMask = index;
        } else if (currentSlot.equals(SLOT_EFFECTS)) {
            currentEffect = index;
        } else {
            currentFilter = index;
        }
    }


    private void gotoNext() {
        ArrayList<AREffect> activeList = getActiveList();
        int index = getActiveIndex();
        index = index+1;
        if (index >= activeList.size()) {
            index = 0;
        }
        setActiveIndex(index);
        deepAR.switchEffect(currentSlot, activeList.get(index).getPath());
    }


    private void gotoNext(int index) {
        ArrayList<AREffect> activeList = getActiveList();
        //int index = getActiveIndex();
        index = index+1;
        if (index >= activeList.size()) {
            index = 0;
        }
        setActiveIndex(index);
        deepAR.switchEffect(currentSlot, activeList.get(index).getPath());
    }


    private void gotoPrevious() {
        ArrayList<AREffect> activeList = getActiveList();
        int index = getActiveIndex();
        index = index-1;
        if (index < 0) {
            index = activeList.size()-1;
        }
        setActiveIndex(index);
        deepAR.switchEffect(currentSlot, activeList.get(index).getPath());
    }


    boolean recording = false;

    private void setupViews() {
        previousMask = (ImageButton)findViewById(R.id.previousMask);
        nextMask = (ImageButton)findViewById(R.id.nextMask);

        radioMasks = (RadioButton)findViewById(R.id.masks);
        radioEffects = (RadioButton)findViewById(R.id.effects);
        radioFilters = (RadioButton)findViewById(R.id.filters);

        arView = (SurfaceView) findViewById(R.id.surface);

        arView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deepAR.onClick();
            }
        });

        arView.getHolder().addCallback(this);

        // Surface might already be initialized, so we force the call to onSurfaceChanged
        arView.setVisibility(View.GONE);
        arView.setVisibility(View.VISIBLE);


        cameraGrabber = new CameraGrabber();

        switch (screenOrientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                cameraGrabber.setScreenOrientation(90);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                cameraGrabber.setScreenOrientation(270);
                break;
            default:
                cameraGrabber.setScreenOrientation(0);
                break;
        }

        cameraGrabber.setResolutionPreset(CameraResolutionPreset.P640x480);
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
                if (recording) {
                    deepAR.resumeVideoRecording();
                }

            }

            @Override
            public void onCameraError(String errorMsg) {
                Log.e(TAG, errorMsg);
            }
        });

        setupEffects();

        screenshotBtn = (ImageButton)findViewById(R.id.recordButton);
        screenshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deepAR.takeScreenshot();

                // Video recording example
                if (recording) {
                    Toast.makeText(getApplicationContext(), "Video saved in phone memory ",Toast.LENGTH_LONG).show();
                    deepAR.stopVideoRecording();
                    recording = false;
                    releaseMediaPlayer();
                    screenshotBtn.setImageResource(android.R.drawable.ic_menu_upload);
                    isUpload = true;
                   // finish();
                }
                else if (isUpload) {
                    final File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator +"video.mp4");
                    Toast.makeText(getApplicationContext(), "Uploading started",Toast.LENGTH_LONG).show();
                    uploadWithTransferUtility(Environment.getExternalStorageDirectory().toString() + File.separator +"video.mp4",file);

                }
                else {
                    Toast.makeText(getApplicationContext(), "Recording started",Toast.LENGTH_LONG).show();
                   playAudio(whichContent+ ".mp3");
                    screenshotBtn.setImageResource(android.R.drawable.ic_media_pause);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deepAR.startVideoRecording(Environment.getExternalStorageDirectory().toString() + File.separator +"video.mp4", 1f);
                            recording = true;

                        }
                    });
                }
            }
        });



        switchCamera = (ImageButton) findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cameraDevice = cameraGrabber.getCurrCameraDevice() ==  Camera.CameraInfo.CAMERA_FACING_FRONT ?  Camera.CameraInfo.CAMERA_FACING_BACK :  Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraGrabber.changeCameraDevice(cameraDevice);
            }
        });

        previousMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoPrevious();
            }
        });

        nextMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNext(0);
            }
        });

        radioMasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioFilters.setChecked(false);
                radioButtonClicked();
            }
        });
        radioEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMasks.setChecked(false);
                radioFilters.setChecked(false);
                radioButtonClicked();
            }
        });
        radioFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioMasks.setChecked(false);
                radioButtonClicked();
            }
        });

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
    @Override
    public void screenshotTaken(final Bitmap screenshot) {
        CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
        try {
            File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DeepAR_" + now + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            screenshot.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(VideoCameraActivity.this, new String[]{imageFile.toString()}, null, null);
            Toast.makeText(VideoCameraActivity.this, getResources().getString(R.string.screenshot_saved), Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void videoRecordingStarted() {

    }

    @Override
    public void videoRecordingFinished() {
    }

    @Override
    public void videoRecordingFailed() {

    }

    @Override
    public void initialized()

    {

    }

    @Override
    public void faceVisibilityChanged(boolean faceVisible) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        deepAR.setRenderSurface(surfaceHolder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        deepAR.setRenderSurface(null,0,0);
    }

    @Override
    public void videoRecordingPrepared() {}

    @Override
    public void error(String error) {
        if (error.equals(DeepAR.ERROR_EFFECT_FILE_LOAD_FAILED)) {

        } else if (error.equals(DeepAR.ERROR_MODEL_FILE_NOT_FOUND)) {

        }
    }
}
