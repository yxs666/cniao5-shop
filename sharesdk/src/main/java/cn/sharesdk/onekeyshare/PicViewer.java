/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */


package cn.sharesdk.onekeyshare;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.mob.tools.FakeActivity;

/** 查看编辑页面中图片的例子 */
public class PicViewer extends FakeActivity implements OnTouchListener {
	private ImageView ivViewer;
	private Bitmap pic;

	Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    DisplayMetrics dm;

    /** 最小缩放比例*/
    float minScaleR = 1f;
    /** 最大缩放比例*/
    static final float MAX_SCALE = 10f;

    /** 初始状态*/
    static final int NONE = 0;
    /** 拖动*/
    static final int DRAG = 1;
    /** 缩放*/
    static final int ZOOM = 2;

    /** 当前模式*/
    int mode = NONE;

    PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;

	/** 设置图片用于浏览 */
	public void setImageBitmap(Bitmap pic) {
		this.pic = pic;
		if (ivViewer != null) {
			ivViewer.setImageBitmap(pic);
		}
	}

	public void onCreate() {
		ivViewer = new ImageView(activity);
		ivViewer.setScaleType(ScaleType.MATRIX);
		ivViewer.setBackgroundColor(0xc0000000);
		ivViewer.setOnTouchListener(this);
		if (pic != null && !pic.isRecycled()) {
			ivViewer.setImageBitmap(pic);
		}
		dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率
        minZoom();
        CheckView();
        ivViewer.setImageMatrix(matrix);
	    activity.setContentView(ivViewer);

	}



	/**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        // 主点按下
        case MotionEvent.ACTION_DOWN:
            savedMatrix.set(matrix);
            prev.set(event.getX(), event.getY());
            mode = DRAG;
            break;
        // 副点按下
        case MotionEvent.ACTION_POINTER_DOWN:
            dist = spacing(event);
            // 如果连续两点距离大于10，则判定为多点模式
            if (spacing(event) > 10f) {
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - prev.x, event.getY()
                        - prev.y);
            } else if (mode == ZOOM) {
                float newDist = spacing(event);
                if (newDist > 10f) {
                    matrix.set(savedMatrix);
                    float tScale = newDist / dist;
                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                }
            }
            break;
        }
        ivViewer.setImageMatrix(matrix);
        CheckView();
        return true;
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView() {
        float p[] = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
            if (p[0] < minScaleR) {
//                Log.d("", "当前缩放级别:"+p[0]+",最小缩放级别:"+minScaleR);
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
//                Log.d("", "当前缩放级别:"+p[0]+",最大缩放级别:"+MAX_SCALE);
                matrix.set(savedMatrix);
            }
        }
        center();
    }

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        minScaleR = Math.min(
                (float) dm.widthPixels / (float) pic.getWidth(),
                (float) dm.heightPixels / (float) pic.getHeight());
        //以最小缩放比例显示
        matrix.setScale(minScaleR, minScaleR);
    }

    private void center() {
        center(true, true);
    }

    /**
     * 横向、纵向居中
     */
    protected void center(boolean horizontal, boolean vertical) {

    	 Matrix m = new Matrix();
         m.set(matrix);
         RectF rect = new RectF(0, 0, pic.getWidth(), pic.getHeight());
         m.mapRect(rect);

         float height = rect.height();
         float width = rect.width();

         float deltaX = 0, deltaY = 0;

         if (vertical) {
             // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
             int screenHeight = dm.heightPixels;
             if (height < screenHeight) {
                 deltaY = (screenHeight - height) / 2 - rect.top;
             } else if (rect.top > 0) {
                 deltaY = -rect.top;
             } else if (rect.bottom < screenHeight) {
                 deltaY = ivViewer.getHeight() - rect.bottom;
             }
         }

         if (horizontal) {
             int screenWidth = dm.widthPixels;
             if (width < screenWidth) {
                 deltaX = (screenWidth - width) / 2 - rect.left;
             } else if (rect.left > 0) {
                 deltaX = -rect.left;
             } else if (rect.right < screenWidth) {
                 deltaX = ivViewer.getWidth() - rect.right;
             }
         }
         matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 两点的距离
     */
	private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

}
