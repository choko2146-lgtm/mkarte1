package com.example.mkarte1.ui.photo;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.PhotoImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoPreviewActivity extends AppCompatActivity {
    public static final String EXTRA_PHOTO_ID = PhotoDetailActivity.EXTRA_PHOTO_ID;
    private static final float MIN_SCALE = 1f;
    private static final float DOUBLE_TAP_SCALE = 2.5f;
    private static final float MAX_SCALE = 5f;
    private static final float SWIPE_DISTANCE_DP = 72f;
    private static final float SWIPE_VELOCITY_DP = 240f;

    private ImageView imagePhoto;
    private TextView textCustomerName;
    private TextView textPreviewMeta;
    private PhotoRepository repository;
    private final List<Photo> photoList = new ArrayList<>();
    private final Matrix photoMatrix = new Matrix();
    private final RectF photoRect = new RectF();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private int currentIndex = -1;
    private float currentScale = MIN_SCALE;
    private float lastTouchX;
    private float lastTouchY;
    private boolean dragging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo_preview);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        imagePhoto = findViewById(R.id.imagePhotoPreview);
        textCustomerName = findViewById(R.id.textPreviewCustomerName);
        textPreviewMeta = findViewById(R.id.textPreviewMeta);
        imagePhoto.setScaleType(ImageView.ScaleType.MATRIX);
        findViewById(R.id.buttonPreviewBack).setOnClickListener(v -> finish());
        setupGestures();
        enterImmersiveMode();

        repository = new PhotoRepository(this);
        long photoId = getIntent().getLongExtra(EXTRA_PHOTO_ID, -1);
        if (photoId <= 0) {
            Toast.makeText(this, "写真を開けません", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadPhotoAndCustomerPhotos(photoId);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void setupGestures() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scalePhoto(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (currentScale > MIN_SCALE + 0.05f) {
                    resetPhotoMatrix();
                } else {
                    zoomTo(DOUBLE_TAP_SCALE, e.getX(), e.getY());
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return handleSwipe(e1, e2, velocityX, velocityY);
            }
        });

        View.OnTouchListener listener = (v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            handleDrag(event);
            return true;
        };
        imagePhoto.setOnTouchListener(listener);
        findViewById(R.id.rootPhotoPreview).setOnTouchListener(listener);
    }

    private void loadPhotoAndCustomerPhotos(long photoId) {
        repository.get(photoId, value -> {
            if (value == null) {
                Toast.makeText(this, "写真が見つかりません", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            repository.listForCustomer(value.customerId, photos -> {
                photoList.clear();
                if (photos != null) {
                    photoList.addAll(photos);
                }
                currentIndex = findPhotoIndex(photoId);
                if (currentIndex == -1) {
                    photoList.add(value);
                    currentIndex = photoList.size() - 1;
                }
                showPhoto(photoList.get(currentIndex));
            });
        });
    }

    private int findPhotoIndex(long photoId) {
        for (int i = 0; i < photoList.size(); i++) {
            if (photoList.get(i).id == photoId) {
                return i;
            }
        }
        return -1;
    }

    private void showPhoto(Photo photo) {
        if (photo == null) {
            Toast.makeText(this, "写真が見つかりません", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dragging = false;
        updateHeader(photo);
        bindPhotoImage(photo.uri);
    }

    private void updateHeader(Photo photo) {
        textCustomerName.setText(fallback(photo.customerName, "名前未設定"));
        String displayDate = DateUtil.displayYmd(photo.takenDate);
        int total = Math.max(1, photoList.size());
        int position = currentIndex >= 0 ? currentIndex + 1 : 1;
        textPreviewMeta.setText(fallback(displayDate, "撮影日未登録") + "  " + position + " / " + total);
    }

    private boolean handleSwipe(MotionEvent start, MotionEvent end, float velocityX, float velocityY) {
        if (start == null || end == null || currentScale > MIN_SCALE + 0.05f || photoList.size() <= 1) {
            return false;
        }

        float dx = end.getX() - start.getX();
        float dy = end.getY() - start.getY();
        float density = getResources().getDisplayMetrics().density;
        float minDistance = SWIPE_DISTANCE_DP * density;
        float minVelocity = SWIPE_VELOCITY_DP * density;

        if (Math.abs(dx) < minDistance
                || Math.abs(velocityX) < minVelocity
                || Math.abs(dx) < Math.abs(dy) * 1.4f) {
            return false;
        }

        if (dx > 0) {
            return showPreviousPhoto();
        }
        return showNextPhoto();
    }

    private boolean showPreviousPhoto() {
        if (currentIndex > 0) {
            currentIndex--;
            showPhoto(photoList.get(currentIndex));
            return true;
        }
        return false;
    }

    private boolean showNextPhoto() {
        if (currentIndex < photoList.size() - 1) {
            currentIndex++;
            showPhoto(photoList.get(currentIndex));
            return true;
        }
        return false;
    }

    private void bindPhotoImage(String uriText) {
        if (uriText == null || uriText.trim().isEmpty()) {
            imagePhoto.setImageDrawable(null);
            photoMatrix.reset();
            currentScale = MIN_SCALE;
            return;
        }

        try {
            Uri uri = Uri.parse(uriText);
            if ("file".equals(uri.getScheme()) && uri.getPath() != null) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    imagePhoto.post(() -> {
                        if (!PhotoImageLoader.loadFileInto(imagePhoto, file)) {
                            imagePhoto.setImageURI(Uri.fromFile(file));
                        }
                        imagePhoto.post(this::resetPhotoMatrix);
                    });
                    return;
                }
            }
            imagePhoto.setImageURI(uri);
            imagePhoto.post(this::resetPhotoMatrix);
        } catch (Exception ignored) {
            imagePhoto.setImageDrawable(null);
        }
    }

    private void resetPhotoMatrix() {
        Drawable drawable = imagePhoto.getDrawable();
        int viewWidth = imagePhoto.getWidth();
        int viewHeight = imagePhoto.getHeight();
        if (drawable == null || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth <= 0 || drawableHeight <= 0) {
            return;
        }

        float scale = Math.min(
                (float) viewWidth / drawableWidth,
                (float) viewHeight / drawableHeight
        );
        float dx = (viewWidth - drawableWidth * scale) / 2f;
        float dy = (viewHeight - drawableHeight * scale) / 2f;

        photoMatrix.reset();
        photoMatrix.setScale(scale, scale);
        photoMatrix.postTranslate(dx, dy);
        currentScale = MIN_SCALE;
        imagePhoto.setImageMatrix(photoMatrix);
    }

    private void scalePhoto(float scaleFactor, float focusX, float focusY) {
        Drawable drawable = imagePhoto.getDrawable();
        if (drawable == null) {
            return;
        }

        float targetScale = currentScale * scaleFactor;
        targetScale = Math.max(MIN_SCALE, Math.min(targetScale, MAX_SCALE));
        float adjustedFactor = targetScale / currentScale;
        if (adjustedFactor == 1f) {
            return;
        }

        photoMatrix.postScale(adjustedFactor, adjustedFactor, focusX, focusY);
        currentScale = targetScale;
        fixPhotoTranslation();
        imagePhoto.setImageMatrix(photoMatrix);
    }

    private void zoomTo(float targetScale, float focusX, float focusY) {
        targetScale = Math.max(MIN_SCALE, Math.min(targetScale, MAX_SCALE));
        float adjustedFactor = targetScale / currentScale;
        photoMatrix.postScale(adjustedFactor, adjustedFactor, focusX, focusY);
        currentScale = targetScale;
        fixPhotoTranslation();
        imagePhoto.setImageMatrix(photoMatrix);
    }

    private void handleDrag(MotionEvent event) {
        if (event.getPointerCount() > 1 || scaleGestureDetector.isInProgress()) {
            dragging = false;
            return;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                dragging = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragging && currentScale > MIN_SCALE + 0.05f) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    photoMatrix.postTranslate(dx, dy);
                    fixPhotoTranslation();
                    imagePhoto.setImageMatrix(photoMatrix);
                }
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                break;
            default:
                break;
        }
    }

    private void fixPhotoTranslation() {
        Drawable drawable = imagePhoto.getDrawable();
        if (drawable == null) {
            return;
        }

        photoRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        photoMatrix.mapRect(photoRect);

        float deltaX = translationDelta(photoRect.left, photoRect.right, imagePhoto.getWidth());
        float deltaY = translationDelta(photoRect.top, photoRect.bottom, imagePhoto.getHeight());
        photoMatrix.postTranslate(deltaX, deltaY);
    }

    private float translationDelta(float start, float end, float viewSize) {
        float contentSize = end - start;
        if (contentSize <= viewSize) {
            return (viewSize - contentSize) / 2f - start;
        }
        if (start > 0) {
            return -start;
        }
        if (end < viewSize) {
            return viewSize - end;
        }
        return 0f;
    }

    private String fallback(String value, String fallback) {
        String text = value == null ? "" : value.trim();
        return text.isEmpty() ? fallback : text;
    }
}
