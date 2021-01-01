package com.dacho.darkmoon.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.dacho.darkmoon.cache.BitmapCache;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by hirofumi on 15/05/27.
 */
public class BitmapLoading extends AsyncTask<String,Bitmap,Bitmap>{

    private String url;
    private BitmapCache bitmapCache;
    private ImageView imageView;
    private boolean endFlag = false;
    private Bitmap tmp;

    public BitmapLoading(String url,ImageView imageView,BitmapCache bitmapCache){
        this.url = url;
        this.imageView = imageView;
        this.bitmapCache = bitmapCache;
    }

    @Override
    protected void onPreExecute() {
        //ためしに読み込んでみる
        tmp = bitmapCache.getBitmap(url);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        //読み込んだ画像がnullでなかったそれでよい
        if(tmp!=null)
            return getCroppedBitmap(tmp);
        Bitmap bitmap = null;
        try{
            URL imageUrl = new URL(url);
            InputStream inputStream = imageUrl.openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            //ここでbitmap cacheにいれる
            publishProgress(bitmap);
            Log.d("call","end load image");

        }catch(Exception e){
            e.printStackTrace();
        }
        return getCroppedBitmap(bitmap);
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        if(values[0] != null);
            bitmapCache.putBitmap(url,values[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }

    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectf = new RectF(0, 0, width, height);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        //canvas.drawRoundRect(rectf, width / 5, height / 5, paint);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }

    public Bitmap getReductedBitmap(Bitmap bitmap,ImageView imageView){
        int viewWidth = imageView.getMeasuredWidth();
        int viewHeight = imageView.getMeasuredHeight();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix matrix = new Matrix();
        Paint paint = new Paint();
        matrix.postScale(viewWidth / bitmapWidth, viewHeight / bitmapHeight);
        canvas.drawBitmap(bitmap,matrix,paint);
        return output;

    }

}
