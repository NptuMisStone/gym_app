package com.NPTUMisStone.gym_app.User_And_Coach;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.NPTUMisStone.gym_app.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CameraDevice cameraDevice;
    private TextureView textureView;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private Size previewSize;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private boolean isFrontCamera = false;
    private int rotationAngle = 0;
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_and_coach_camera);
        textureView = findViewById(R.id.userInfo_textureView2);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
        });

        Button takePhotoButton = findViewById(R.id.button_take_photo);
        Button returnButton = findViewById(R.id.button_return);
        Button switchCameraButton = findViewById(R.id.button_switch_camera);
        Button rotateButton = findViewById(R.id.button_rotate);

        takePhotoButton.setOnClickListener(v -> takePhoto());
        returnButton.setOnClickListener(v -> finish());
        switchCameraButton.setOnClickListener(v -> switchCamera());
        rotateButton.setOnClickListener(v -> rotateCameraPreview());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private ImageReader imageReader;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
        stopBackgroundThread();
    }

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e("CameraActivity", "Error stopping background thread", e);
            }
        }
    }

    private void switchCamera() {
        isFrontCamera = !isFrontCamera;
        closeCamera();
        openCamera();
    }

    private void rotateCameraPreview() {
        rotationAngle = (rotationAngle + 90) % 360;
        textureView.setRotation(rotationAngle);
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[isFrontCamera ? 1 : 0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = Objects.requireNonNull(map).getOutputSizes(SurfaceTexture.class)[0];

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                return;
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("CameraActivity", "Error accessing camera", e);
        }
    }
    private void startCameraPreview() {
        if (textureView.isAvailable()) {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture != null) {
                setupSurface(texture);
                try {
                    setupCaptureRequest();
                    createCaptureSession();
                } catch (CameraAccessException e) {
                    Log.e("CameraActivity", "Error creating capture session", e);
                }
            }
        }
    }

    private void setupSurface(SurfaceTexture texture) {
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
    }

    private void setupCaptureRequest() throws CameraAccessException {
        Surface surface = new Surface(textureView.getSurfaceTexture());
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
    }

    private void createCaptureSession() throws CameraAccessException {
        Surface surface = new Surface(textureView.getSurfaceTexture());
        cameraDevice.createCaptureSession(List.of(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) return;
                captureSession = session;
                updatePreview();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void updatePreview() {
        if (cameraDevice == null) return;
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e("CameraActivity", "Error updating preview", e);
        }
    }
    private void takePhoto() {
        if (cameraDevice == null) return;

        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(SurfaceTexture.class);
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(imageReader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = createCaptureRequest();
            final File file = new File(getExternalFilesDir(null), "pic.jpg");

            imageReader.setOnImageAvailableListener(createImageAvailableListener(file), mBackgroundHandler);
            cameraDevice.createCaptureSession(outputSurfaces, createCaptureSessionStateCallback(captureBuilder, file), mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e("CameraActivity", "Error taking photo", e);
        }
    }

    private CaptureRequest.Builder createCaptureRequest() throws CameraAccessException {
        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        // Orientation
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
        return captureBuilder;
    }

    private ImageReader.OnImageAvailableListener createImageAvailableListener(final File file) {
        return reader -> {
            mBackgroundHandler.post(() -> {
                try (Image image = reader.acquireLatestImage()) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    saveImage(bytes, file);
                    runOnUiThread(() -> {
                        setResult(RESULT_OK, new Intent().putExtra("imagePath", file.getAbsolutePath()));
                        finish();
                    });
                } catch (FileNotFoundException e) {
                    Log.e("CameraActivity", "File not found", e);
                } catch (IOException e) {
                    Log.e("CameraActivity", "Error saving file", e);
                }
            });
        };
    }

    private void saveImage(byte[] bytes, File file) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle); // Apply the rotation angle
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        try (OutputStream output = new FileOutputStream(file)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        }
    }

    private CameraCaptureSession.StateCallback createCaptureSessionStateCallback(final CaptureRequest.Builder captureBuilder, final File file) {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), createCaptureCallback(file), mBackgroundHandler);
                } catch (CameraAccessException e) {
                    Log.e("CameraActivity", "Error taking photo", e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            }
        };
    }

    private CameraCaptureSession.CaptureCallback createCaptureCallback(final File file) {
        return new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imagePath", file.getAbsolutePath());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                new AlertDialog.Builder(this).setTitle("Error")
                        .setMessage("Cannot open camera, please check your camera app or permission settings.")
                        .setPositiveButton("OK", null).show();
            }
        }
    }
}