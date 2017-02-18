package newera.myapplication.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import newera.myapplication.R;
import newera.myapplication.image.Image;
import newera.myapplication.ui.system.PictureFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emile Barjou-Suire on 09/02/2017.
 */

public class CImageView extends View {
    private final static float MOVE_SAFEZONE = 0.5f;
    private final static float LERP_FACTOR = 3f;
    private enum TouchMethod {DRAG, ZOOM, TOOL}
    private Image image;
    private Point contentCoords;
    private float contentScale;
    private TouchHandler touchHandler;
    private Rect src;
    private Rect dst;

    public CImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        image = null;
        this.contentCoords = new Point(0, 0);
        this.touchHandler = new TouchHandler();
        this.contentScale = 1f;
        this.src = new Rect();
        this.dst = new Rect();
    }

    /**
     * Set the picture to be displayed on the view.
     * @param image the Image object to be displayed.
     */
    public void setImage(Image image)
    {
        if(!image.isEmpty()) {
            this.image = image;
            //src = new Rect(0, 0, image.getWidth(), image.getHeight());
            //dst = new Rect(getWidth() - image.getWidth() / 2, getHeight() - image.getHeight() / 2, getWidth() + image.getWidth() / 2, getHeight() + image.getHeight() / 2);
            contentCoords.x = getWidth() / 2;
            contentCoords.y = getHeight() / 2;
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawColor(getResources().getColor(R.color.colorPrimaryDark));
        if (image != null && !image.isEmpty()){
            /*
             * Will be moved to Image.draw(canvas, x, y)
             */
            for(int y = 0; y < image.getTileH(); ++y) {
                for (int x = 0; x < image.getTileW(); ++x) {
                    src.set(0, 0, image.getWidth(x,y)-1, image.getHeight(x,y)-1);
                    dst.left = (contentCoords.x - (int)(image.getWidth() * (contentScale/2))) + (int)(x*(PictureFileManager.DECODE_TILE_SIZE-1)*(contentScale));
                    dst.top = (contentCoords.y - (int)(image.getHeight() * (contentScale/2))) + (int)(y*(PictureFileManager.DECODE_TILE_SIZE-1)*(contentScale));
                    dst.right = dst.left + (int)((image.getWidth(x,y))*(contentScale));
                    dst.bottom = dst.top + (int)((image.getHeight(x,y))*(contentScale));
                    /*dst.right = contentCoords.x + (int) (image.getWidth() * (contentScale/2));
                    dst.bottom =  contentCoords.y + (int) (image.getHeight() * (contentScale/2));*/
                    canvas.drawBitmap(image.getBitmap(x, y), src, dst, null);
                }
            }
            /*dst.left = contentCoords.x - (int) (image.getWidth() * (contentScale/2));
            dst.top = contentCoords.y - (int) (image.getHeight() * (contentScale/2));
            dst.right = contentCoords.x + (int) (image.getWidth() * (contentScale/2));
            dst.bottom =  contentCoords.y + (int) (image.getHeight() * (contentScale/2));
            canvas.drawBitmap(image.getBitmap(), src, dst, null);*/
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getPointerCount() <= 1)
        {
            Log.i("DBG", "("+contentCoords.x+","+contentCoords.y+")");
            contentScale = touchHandler.onTouch(event, TouchMethod.DRAG, contentCoords, contentScale);
            /*contentCoords.x = (int) (Math.min(contentCoords.x, getWidth() * MOVE_SAFEZONE + (int) (image.getWidth() * (contentScale/2))));      // need the scale factor
            contentCoords.y = (int) (Math.min(contentCoords.y, getHeight() * MOVE_SAFEZONE + (int) (image.getHeight() * (contentScale/2))));   // somewhere here
            */
        }else{
            contentScale = touchHandler.onTouch(event, TouchMethod.ZOOM, contentCoords, contentScale);
        }

        invalidate();
        return true;
    }

    private class TouchHandler{
        private int initialX, initialY;
        private int initialContentX, initialContentY;
        private float initialDist, initialScale;
        private TouchMethod method;
        private int mActivePointerId, pointerIndex;
        private List<Point> touchList;


        TouchHandler(){
            this.touchList = new ArrayList<Point>();
            this.touchList = new ArrayList<Point>();
        }

        float onTouch(MotionEvent event, TouchMethod method, Point coord, float scale){
            touchList.clear();
            for(int i = 0; i < event.getPointerCount(); ++i){
                mActivePointerId = event.getPointerId(i);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                touchList.add( new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex)) );
            }

            switch(method) {
                case DRAG: {
                    initialDist = -1f;
                    initialScale = scale;
                    switch(event.getAction()) {

                        case MotionEvent.ACTION_DOWN: {
                            initialX = touchList.get(0).x;
                            initialY = touchList.get(0).y;
                            initialContentX = coord.x;
                            initialContentY = coord.y;
                        } break;

                        case MotionEvent.ACTION_MOVE: {
                            /*coord.x = (int) Math.max(0 - image.getWidth() * contentScale + getWidth() * MOVE_SAFEZONE  + (int) (image.getWidth() * (contentScale/2)), initialContentX + (touchList.get(0).x - initialX)); // need a scale factor somewhere here
                            coord.y = (int) Math.max(0 - image.getHeight() * contentScale + getHeight() * MOVE_SAFEZONE + (int) (image.getHeight() * (contentScale/2)), initialContentY + (touchList.get(0).y - initialY));
                            */
                            coord.x = initialContentX + (touchList.get(0).x - initialX); // need a scale factor somewhere here
                            coord.y = initialContentY + (touchList.get(0).y - initialY);
                        } break;
                    }
                } break;

                case ZOOM: {
                    if (initialDist < 0)
                    {
                        initialDist = touchList.get(0).distanceFromPoint(touchList.get(1));
                    }else{
                        float currentDist = touchList.get(0).distanceFromPoint(touchList.get(1));
                        scale = currentDist / initialDist * initialScale;
                    }

                } break;

                case TOOL: {
                } break;
            }

            return scale;
        }

    }

    private class Point{
        int x, y;

        Point(){
            this.x = 0;
            this.y = 0;
        }

        Point(int x, int y){
            this.x = x;
            this.y = y;
        }

        float distanceFromPoint(Point b) {
            return (float) Math.sqrt((double)((this.x - b.x)*(this.x - b.x) + (this.y - b.y)*(this.y - b.y)));
        }
    }
}
