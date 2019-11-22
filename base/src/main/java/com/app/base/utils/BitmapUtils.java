package com.app.base.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BitmapUtils {

    /**
     * 给一张Bitmap添加水印文字。
     *
     * @param bitmap      源图片
     * @param content  水印文本
     * @param textSize 水印字体大小 ，单位pix。
     * @param color    水印字体颜色。
     * @param x        起始坐标x
     * @param y        起始坐标y
     *@param positionFlag  居左/居右
     * @param recycle  是否回收
     * @return 已经添加水印后的Bitmap。
     */
    public static Bitmap addTextWatermark(Bitmap bitmap, String content, int textSize, int color, float x, float y, boolean positionFlag, boolean recycle) {
        if (isEmptyBitmap(bitmap) || content == null)
            return null;
        Bitmap ret = bitmap.copy(bitmap.getConfig(), true);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(ret);
        paint.setColor(color);
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(content, 0, content.length(), bounds);
        canvas.drawText(content, bitmap.getWidth() - x - bounds.width() - bounds.left, bitmap.getHeight() - bounds.height() - bounds.top - y, paint);

        if (positionFlag) {
            canvas.drawText(content, x, bitmap.getHeight() - bounds.height() - bounds.top - y, paint);
        } else {
            canvas.drawText(content, bitmap.getWidth() - x - bounds.width() - bounds.left, bitmap.getHeight() - bounds.height() - bounds.top - y, paint);

        }
        if (recycle && !bitmap.isRecycled())
            bitmap.recycle();
        return ret;
    }

    /**
     * Bitmap对象是否为空。
     */
    public static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }


    /**
     * 给图片添加水印，水印会根据图片宽高自动缩放处理
     *
     * @param watermark              水印
     * @param image                  添加水印的图片
     * @param offsetX                添加水印的X轴偏移量
     * @param offsetY                添加水印的Y轴偏移量
     * @param srcWaterMarkImageWidth 水印对应的原图片宽度,即ui制作水印时候参考的图片画布宽度,应该是已知的图片最大宽度
     * @param addInLeft              true 在左下角添加水印，false 在右下角添加水印
     */
    public static void addWatermark(Bitmap watermark, Bitmap image, int srcWaterMarkImageWidth, int offsetX, int offsetY, boolean addInLeft) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if (0 == imageWidth || 0 == imageHeight) {
            throw new RuntimeException("AlbumBuilder: 加水印的原图宽或高不能为0！");
        }
        int watermarkWidth = watermark.getWidth();
        int watermarkHeight = watermark.getHeight();
        float scale = imageWidth / (float) srcWaterMarkImageWidth;
        if (scale > 1) scale = 1;
        else if (scale < 0.4) scale = 0.4f;
        int scaleWatermarkWidth = (int) (watermarkWidth * scale);
        int scaleWatermarkHeight = (int) (watermarkHeight * scale);
        Bitmap scaleWatermark = Bitmap.createScaledBitmap(watermark, scaleWatermarkWidth, scaleWatermarkHeight, true);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (addInLeft) {
            canvas.drawBitmap(scaleWatermark, offsetX, imageHeight - scaleWatermarkHeight - offsetY, paint);
        } else {
            canvas.drawBitmap(scaleWatermark, imageWidth - offsetX - scaleWatermarkWidth, imageHeight - scaleWatermarkHeight - offsetY, paint);
        }

        scaleWatermark.recycle();
    }

    /**
     * 给图片添加带文字和图片的水印，水印会根据图片宽高自动缩放处理
     *
     * @param watermark              水印图片
     * @param image                  要加水印的图片
     * @param srcWaterMarkImageWidth 水印对应的原图片宽度,即ui制作水印时候参考的图片画布宽度,应该是已知的图片最大宽度
     * @param text                   要添加的文字
     * @param offsetX                添加水印的X轴偏移量
     * @param offsetY                添加水印的Y轴偏移量
     * @param addInLeft              true 在左下角添加水印，false 在右下角添加水印
     */
    public static void addWatermarkWithText(@NonNull Bitmap watermark, Bitmap image, int srcWaterMarkImageWidth, @NonNull String text, int offsetX, int offsetY, boolean addInLeft) {
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        if (0 == imageWidth || 0 == imageHeight) {
            throw new RuntimeException("AlbumBuilder: 加水印的原图宽或高不能为0！");
        }
        float watermarkWidth = watermark.getWidth();
        float watermarkHeight = watermark.getHeight();
        float scale = imageWidth / (float) srcWaterMarkImageWidth;
        if (scale > 1) scale = 1;
        else if (scale < 0.4) scale = 0.4f;
        float scaleWatermarkWidth = watermarkWidth * scale;
        float scaleWatermarkHeight = watermarkHeight * scale;
        Bitmap scaleWatermark = Bitmap.createScaledBitmap(watermark, (int) scaleWatermarkWidth, (int) scaleWatermarkHeight, true);
        Canvas canvas = new Canvas(image);
        Paint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        float textSize = (float) (scaleWatermark.getHeight() * 2) / (float) 3;
        textPaint.setTextSize(textSize);
        Rect textRect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textRect);
        if (addInLeft) {
            canvas.drawText(text, scaleWatermarkWidth + offsetX, imageHeight - textRect.height() - textRect.top - offsetY, textPaint);
        } else {
            canvas.drawText(text, imageWidth - offsetX - textRect.width() - textRect.left, imageHeight - textRect.height() - textRect.top - offsetY, textPaint);
        }

        Paint scaleWatermarkPaint = new Paint();
        scaleWatermarkPaint.setAntiAlias(true);
        if (addInLeft) {
            canvas.drawBitmap(scaleWatermark, offsetX, imageHeight - textRect.height() - offsetY - scaleWatermarkHeight / 6, scaleWatermarkPaint);
        } else {
            canvas.drawBitmap(scaleWatermark, imageWidth - textRect.width() - offsetX - scaleWatermarkWidth / 6, imageHeight - textRect.height() - offsetY - scaleWatermarkHeight / 6, scaleWatermarkPaint);
        }
        scaleWatermark.recycle();
    }


    public static Bitmap getRecyclerViewScreenshot(RecyclerView view) {
        int size = view.getAdapter().getItemCount();
        RecyclerView.ViewHolder holder = view.getAdapter().createViewHolder(view, 0);
        view.getAdapter().onBindViewHolder(holder, 0);
        ArrayList<Bitmap> bitmaps=new ArrayList<>();
        holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(),
                View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
        Bitmap bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), holder.itemView.getMeasuredHeight() * size,
                Bitmap.Config.ARGB_8888);
        Canvas bigCanvas = new Canvas(bigBitmap);
        bigCanvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        int iHeight = 0;
        holder.itemView.setDrawingCacheEnabled(true);
        holder.itemView.buildDrawingCache();
        bigCanvas.drawBitmap(holder.itemView.getDrawingCache(), 0f, iHeight, paint);
        holder.itemView.setDrawingCacheEnabled(false);
        holder.itemView.destroyDrawingCache();
        iHeight += holder.itemView.getMeasuredHeight();
        for (int i = 1; i < size; i++) {
            view.getAdapter().onBindViewHolder(holder, i);
            holder.itemView.setDrawingCacheEnabled(true);
            holder.itemView.buildDrawingCache();
            bitmaps.add(holder.itemView.getDrawingCache());
            bigCanvas.drawBitmap(holder.itemView.getDrawingCache(), 0f, iHeight, paint);
            iHeight += holder.itemView.getMeasuredHeight();
            holder.itemView.setDrawingCacheEnabled(false);
            holder.itemView.destroyDrawingCache();
        }
        return bigBitmap;
    }
}
