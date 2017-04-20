package newera.EliJ.image.processing.shaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;

import newera.EliJ.R;
import newera.EliJ.ScriptC_cartoon_edge;
import newera.EliJ.ScriptC_cartoon_saturation;
import newera.EliJ.ScriptC_invert;
import newera.EliJ.ScriptC_soustract;
import newera.EliJ.ScriptC_thresholdLightness;
import newera.EliJ.image.Image;
import newera.EliJ.image.processing.EItems;


/**
 * Created by romain on 09/02/17.
 */

public class Pencil extends Shader{

    public Pencil(Context context) {
        super(context);
        this.drawableIconId = R.drawable.ic_contrast_tonality_black_24dp;
        this.clickableName = R.string.shaderPencilName;
        this.item = EItems.F_PENCIL;
    }

    private float factor_edge = 1;
    private float[][] matrix_edge = {
            {0,0,-1,0,0},
            {0,-1,-2,-1,0},
            {-1,-2,16,-2,-1},
            {0,-1,-2,-1,0},
            {0,0,-1,0,0},
    };

    @Override
    public void ApplyFilter(Image image)
    {
        if(image != null && !image.isEmpty()) {

            ScriptC_cartoon_edge rsConv = new ScriptC_cartoon_edge(renderScript);

            for (Bitmap[] arrBitmap : image.getBitmaps())
                for (Bitmap bitmap : arrBitmap) {


                    Allocation in = Allocation.createFromBitmap(renderScript, bitmap);
                    Allocation out = Allocation.createTyped(renderScript, in.getType());
                    Allocation out2 = Allocation.createTyped(renderScript, in.getType());

                    ScriptC_thresholdLightness rsLum = new ScriptC_thresholdLightness(renderScript);
                    rsLum.forEach_treshold(in, out);//

                    rsConv.set_h(bitmap.getHeight());
                    rsConv.set_w(bitmap.getWidth());
                    rsConv.set_in(out);

                    rsConv.set_matrix_size(matrix_edge.length);
                    rsConv.set_matrix_factor(1);
                    float[] mat = MatrixToArray(matrix_edge);
                    Allocation matAlloc = Allocation.createSized(renderScript, Element.F32(renderScript), mat.length);
                    matAlloc.copyFrom(mat);
                    rsConv.set_matrix2D(matAlloc);

                    rsConv.forEach_cartoon_edge(out2);//

                    ScriptC_invert rsInv = new ScriptC_invert(renderScript);
                    rsInv.forEach_invert(out2, in);//

                    in.copyTo(bitmap);
                }
        }
    }


    private float[] MatrixToArray(float[][] mat){
        float arr[] = new float[mat.length * mat[0].length];
        int i = 0;
        for(int y = 0; y < mat.length; ++y){
            for(int x = 0; x < mat[0].length; ++x){
                arr[i++] = mat[x][y];
            }
        }
        return arr;
    }

}