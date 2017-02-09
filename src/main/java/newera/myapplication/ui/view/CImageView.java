package newera.myapplication.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import newera.myapplication.R;
import newera.myapplication.image.Image;

import java.util.ResourceBundle;

/**
 * Created by Emile Barjou-Suire on 09/02/2017.
 */

public class CImageView extends View {
    private Image image;

    public CImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        image = null;
    }

    public void setImage(Image image)
    {
        this.image = image;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        if (image == null || image.isEmpty()){
            canvas.drawColor(getResources().getColor(R.color.colorPrimary));
        } else {
            canvas.drawBitmap(image.getBitmap(), 0, 0, null);
        }
    }


}
