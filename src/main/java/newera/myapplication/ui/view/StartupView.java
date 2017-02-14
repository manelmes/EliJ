package newera.myapplication.ui.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.View;
import newera.myapplication.R;

/**
 * Created by echo on 14/02/2017.
 */
public class StartupView extends View {

    private final static float SPLIT_RATIO = 0.5f;
    private final static int ICON_SIZE = 100;
    private final static int TEXT_SIZE = 40;

    private Paint paint;
    private boolean isActive = true;

    private Drawable galleryIcon;
    private Drawable cameraIcon;
    private Bitmap galleryIconBitmap;
    private Bitmap cameraIconBitmap;
    private Canvas galleryIconCanvas;
    private Canvas cameraIconCanvas;

    public StartupView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_SIZE);
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        galleryIcon = getResources().getDrawable(R.drawable.ic_collections_black_24dp);
        cameraIcon = getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp);

        galleryIconBitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        galleryIconCanvas = new Canvas(galleryIconBitmap);

        cameraIconBitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        cameraIconCanvas = new Canvas(cameraIconBitmap);

        invalidate();
    }

    public void mask()
    {
        this.setActivated(false);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        float centerUp = canvas.getHeight() * SPLIT_RATIO / 2;
        float centerDown = canvas.getHeight() * (1 + SPLIT_RATIO) / 2; //?????????

        canvas.drawLine(0f, canvas.getHeight() * SPLIT_RATIO, canvas.getWidth(), canvas.getHeight() * SPLIT_RATIO, this.paint);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(getResources().getText(R.string.startupGalleryHint).toString(), canvas.getWidth() / 2, centerUp, this.paint);
        canvas.drawText(getResources().getText(R.string.startupCameraHint).toString(), canvas.getWidth() / 2, centerDown, this.paint);

        galleryIcon.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        galleryIcon.setBounds(0,0,ICON_SIZE,ICON_SIZE);
        galleryIcon.draw(galleryIconCanvas);
        canvas.drawBitmap(galleryIconBitmap, canvas.getWidth() / 2 - ICON_SIZE / 2, centerUp - ICON_SIZE - TEXT_SIZE, this.paint);

        cameraIcon.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        cameraIcon.setBounds(0,0,ICON_SIZE,ICON_SIZE);
        cameraIcon.draw(cameraIconCanvas);
        canvas.drawBitmap(cameraIconBitmap, canvas.getWidth() / 2 - ICON_SIZE / 2, centerDown - ICON_SIZE - TEXT_SIZE, this.paint);

    }
}