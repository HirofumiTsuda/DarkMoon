package com.dacho.darkmoon.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.dacho.darkmoon.layoutFragment.LoadFragment;

/**
 * Created by hirofumi on 15/05/27.
 */
public class BitmapCache {

    private static LruCache<String,Bitmap> mMemoryCache;

    public BitmapCache() {

        if (mMemoryCache == null) {

            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;       // 最大メモリに依存した実装
            // int cacheSize = 5 * 1024 * 1024;  // 5MB


    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // 使用キャッシュサイズ(ここではKB単位)
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            // または bitmap.getByteCount() / 1024を利用
        }
    };

}
}

public Bitmap getBitmap(String url) {
        Log.d("call","get bitmap");
synchronized (mMemoryCache) {
        return mMemoryCache.get(url);
        }
        }
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d("call","set bitmap");
        synchronized (mMemoryCache) {
            mMemoryCache.put(url, bitmap);
            // オブジェクトの解放処理が必要なら以下のように実施
        }
    }


}
