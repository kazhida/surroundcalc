package com.abplus.surroundcalc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.abplus.surroundcalc.models.Drawing;
import com.abplus.surroundcalc.models.FreeHand;
import com.abplus.surroundcalc.models.Stroke;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ぐにぐに描くビュー
 *
 * Created by kazhida on 2013/12/27.
 */
public class DoodleView extends SurfaceView implements SurfaceHolder.Callback {

//    public enum KeyColor {
//        BLUE,
//        GREEN,
//        RED,
//        YELLOW
//    }

    @Nullable
    private Stroke stroke;

    @Nullable
    private Drawing drawing;

    @NotNull
    private Drawer drawer = new Drawer();


    @SuppressWarnings("unused")
    public DoodleView(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public DoodleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void surfaceCreated(@NotNull SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(@NotNull SurfaceHolder holder, int format, int width, int height) {
        redraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stroke = new Stroke(new PointF(event.getX(), event.getY()), null);
                redraw();
                return true;
            case MotionEvent.ACTION_MOVE:
                stroke = new Stroke(new PointF(event.getX(), event.getY()), stroke);
                redraw();
                return true;
            case MotionEvent.ACTION_UP:
                if (drawing != null && stroke != null) {
                    drawing.detect(stroke);
                }
                stroke = null;
                redraw();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void clear() {
        if (drawing != null) {
            drawing.clear();
            redraw();
        }
    }

    public void redraw() {
        SurfaceHolder holder = getHolder();
        if (holder != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                try {
                    drawer.draw(canvas);
                } finally {
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setDrawing(@Nullable Drawing drawing) {
        this.drawing = drawing;
        if (drawing != null) {
            drawer.setKeyColor(drawing.getKeyColor());
        }
    }

    private class Drawer {

        private Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint highlightedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int background;
        private float density = getDensity();

        Drawer() {
            initStrokePaint(drawPaint, R.color.liveStroke, 3.0f);
            initStrokePaint(strokePaint, R.color.holdStroke, 3.0f);
            initFillPaint(regionPaint, R.color.region_blue);
            initFillPaint(highlightedPaint, R.color.highlight_blue);
        }

        private void initStrokePaint(Paint paint, int colorId, float width) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getColor(colorId));
            paint.setStrokeWidth(width * density);
        }

        private void initFillPaint(Paint paint, int colorId) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getColor(colorId));
        }

        private float getDensity() {
            Resources resources = getResources();
            if (resources != null) {
                return resources.getDisplayMetrics().density;
            } else {
                return 1.0f;
            }
        }

        private int getColor(int colorId) {
            Resources resources = getResources();
            if (resources != null) {
                return resources.getColor(colorId);
            } else {
                return Color.WHITE;
            }
        }

        void setKeyColor(Drawing.KeyColor keyColor) {
            switch (keyColor) {
                case BLUE:
                    background = getColor(R.color.bg_blue);
                    regionPaint.setColor(getColor(R.color.region_blue));
                    highlightedPaint.setColor(getColor(R.color.highlight_blue));
                    break;
                case GREEN:
                    background = getColor(R.color.bg_green);
                    regionPaint.setColor(getColor(R.color.region_green));
                    highlightedPaint.setColor(getColor(R.color.highlight_green));
                    break;
                case RED:
                    background = getColor(R.color.bg_red);
                    regionPaint.setColor(getColor(R.color.region_red));
                    highlightedPaint.setColor(getColor(R.color.highlight_red));
                    break;
                case YELLOW:
                    background = getColor(R.color.bg_yellow);
                    regionPaint.setColor(getColor(R.color.region_blue));
                    highlightedPaint.setColor(getColor(R.color.highlight_red));
                    break;
            }
            redraw();
        }

        void draw(Canvas canvas) {
            canvas.drawColor(background);





            if (drawing != null) {
                //  フリーハンドのストローク
                for (FreeHand freeHand: drawing.getFreeHands()) {
                    canvas.drawPath(freeHand.getPath(), strokePaint);
                }

                //  現在のストローク
                for (Stroke s = stroke; s != null; s = s.getTail()) {
                    Stroke tail = s.getTail();
                    if (tail != null) {
                        PointF p0 = tail.getPoint();
                        PointF p1 = s.getPoint();
                        canvas.drawLine(p0.x, p0.y, p1.x, p1.y, drawPaint);
                    }
                }
            }
        }
    }
}
