package com.abplus.surroundcalc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import com.abplus.surroundcalc.models.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ぐにぐに描くビュー
 *
 * Created by kazhida on 2013/12/27.
 */
public class DoodleView extends SurfaceView implements SurfaceHolder.Callback {

    @Nullable
    private Drawing drawing;

    @Nullable
    private Pickable selected;

    @Nullable
    private Stroke stroke;

    @NotNull
    private Drawer drawer = new Drawer();


    @SuppressWarnings("unused")
    public DoodleView(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public DoodleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        if (holder != null) {
            holder.addCallback(this);
        }
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setFocusable(true);
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
                if (drawing != null) {
                    selected = drawing.pick(new PointF(getX(), getY()));
                }
                if (selected == null) {
                    stroke = new Stroke(new PointF(event.getX(), event.getY()), null);
                }
                Log.d("SurroundCALC", "origin: (" + event.getX() + ", " + event.getY());
                redraw();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (selected != null) {
                    selected.moveTo(new PointF(getX(), getY()));
                } else if (stroke != null) {
                    stroke = new Stroke(new PointF(event.getX(), event.getY()), stroke);
                }
                Log.d("SurroundCALC", "move: (" + event.getX() + ", " + event.getY());
                redraw();
                return true;
            case MotionEvent.ACTION_UP:
                Log.d("SurroundCALC", "end: (" + event.getX() + ", " + event.getY());
                if (selected != null) {
                    selected.moveTo(new PointF(getX(), getY()));
                } else if (stroke != null) {
                    if (stroke.isEmpty()) {
                        Log.d("SurroundCALC", "empty stroke");
                        if (event.getDownTime() < ViewConfiguration.getLongPressTimeout()) {
                            callOnClick();
                        } else {
                            performLongClick();
                        }
                    } else if (drawing != null) {
                        Log.d("SurroundCALC", "DETECT");
                        drawing.detect(stroke);
                        Log.d("SurroundCALC", "DETECT done");
                    }
                    stroke = null;
                    redraw();
                }
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

    @Nullable
    public Drawing getDrawing() {
        return drawing;
    }

    private class Drawer {

        private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int background;
        private float density = getDensity();

        Drawer() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(getColor(R.color.liveStroke));
            strokePaint.setStrokeWidth(3.0f * density);
            regionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            regionPaint.setColor(Color.WHITE);
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
                    break;
                case GREEN:
                    background = getColor(R.color.bg_green);
                    break;
                case RED:
                    background = getColor(R.color.bg_red);
                    break;
                case YELLOW:
                    background = getColor(R.color.bg_yellow);
                    break;
            }
            redraw();
        }

        void draw(Canvas canvas) {
            canvas.drawColor(background);





            if (drawing != null) {



                //  リージョン
                for (Region region: drawing.getRegions()) {
                    if (region == selected) {
                        regionPaint.setAlpha(72);
                    } else {
                        regionPaint.setAlpha(255);
                    }
                    canvas.drawPath(region.getPath(), regionPaint);
                }

                //  フリーハンドのストローク
                strokePaint.setAlpha(72);
                for (FreeHand freeHand: drawing.getFreeHands()) {
                    canvas.drawPath(freeHand.getPath(), strokePaint);
                }

                //  現在のストローク
                strokePaint.setAlpha(255);
                for (Stroke s = stroke; s != null; s = s.getTail()) {
                    Stroke tail = s.getTail();
                    if (tail != null) {
                        PointF p0 = tail.getPoint();
                        PointF p1 = s.getPoint();
                        canvas.drawLine(p0.x, p0.y, p1.x, p1.y, strokePaint);
                    }
                }
            }
        }
    }
}
