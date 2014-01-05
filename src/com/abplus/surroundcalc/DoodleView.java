package com.abplus.surroundcalc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import com.abplus.surroundcalc.models.*;
import com.abplus.surroundcalc.models.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ぐにぐに描くビュー
 *
 * Created by kazhida on 2013/12/27.
 */
public final class DoodleView extends SurfaceView implements SurfaceHolder.Callback {

    @Nullable
    private Drawing drawing;

    @Nullable
    private Pickable picked;

    @Nullable
    private Pickable hover;

    @Nullable
    private PointF tapped;


    @Nullable
    private Stroke stroke;

    private int moveCount;
    private boolean mark;

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
        PointF p = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveCount = 0;
                tapped = new PointF(p.x, p.y);
                if (drawing != null && picked == null) {
                    picked = drawing.pick(tapped);
                }
                if (picked == null) {
                    stroke = new Stroke(tapped, null);
                }
                redraw();
                return true;
            case MotionEvent.ACTION_MOVE:
                moveCount++;
                if (picked != null) {
                    picked.moveTo(p);
                    if (drawing != null) {
                        drawing.unbind(picked);
                        hover = drawing.pick(p);
                    }
                }
                if (stroke != null) {
                    stroke = new Stroke(p, stroke);
                }
                redraw();
                return true;
            case MotionEvent.ACTION_UP:
                if (moveCount < 2) {
                    if (picked != null && drawing != null) {
                        drawing.bind(picked);
                    }
                    long pressed = event.getEventTime() - event.getDownTime();
                    if (pressed < ViewConfiguration.getLongPressTimeout()) {
                        callOnClick();
                    } else {
                        performLongClick();
                    }
                } else if (picked != null) {
                    picked.moveTo(p);
                } else if (stroke != null && drawing != null) {
                    Log.d("SurroundCALC", "DETECT");
                    drawing.detect(stroke);
                    Log.d("SurroundCALC", "DETECT done");
                }
                stroke = null;
                picked = null;
                hover = null;
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

    @Nullable
    public Drawing getDrawing() {
        return drawing;
    }

    public void addLabel(@NotNull PointF point, double value) {
        if (drawing != null) {
            drawing.addLabel(point, value, drawer.valuePaint);
        }
    }

    public void addResultLabel(@NotNull PointF point, double value) {
        addLabel(point, value);
    }

    @NotNull
    public PointF getTapped() {
        if (tapped != null) {
            return new PointF(tapped.x, tapped.y);
        } else {
            return new PointF(0, 0);
        }
    }

    @Nullable
    public Pickable getPicked() {
        return picked;
    }

    public void undo() {
        if (drawing != null) {
            drawing.undo();
            redraw();
        }
    }

    public void setMark() {
        mark = true;
    }


    public void resetMark() {
        mark = false;
    }

    private class Drawer {

        private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int background;
        private float density = getDensity();

        Drawer() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(getColor(R.color.liveStroke));
            strokePaint.setStrokeWidth(3.0f * density);

            regionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            regionPaint.setColor(Color.WHITE);

            valuePaint.setStyle(Paint.Style.FILL);
            valuePaint.setColor(getColor(R.color.textColor));
            valuePaint.setTextAlign(Paint.Align.CENTER);
            valuePaint.setTextSize(24.0f * density);

            markerPaint.setStyle(Paint.Style.STROKE);
            markerPaint.setColor(Color.RED);
            markerPaint.setStrokeWidth(1.0f * density);
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
                    if (region == picked || region == hover) {
                        regionPaint.setAlpha(255);
                    } else {
                        regionPaint.setAlpha(153);
                    }
                    canvas.drawPath(region.getPath(), regionPaint);
                }
                //  ラベル
                for (ValueLabel label: drawing.getValueLabels()) {
                    PointF center = label.getCenterPoint();
                    canvas.drawText(label.getText(), center.x, center.y, valuePaint);
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

                if (mark && tapped != null) {
                    float dx = 8.0f * density;
                    canvas.drawLine(tapped.x - dx, tapped.y - dx, tapped.x + dx, tapped.y + dx, markerPaint);
                    canvas.drawLine(tapped.x - dx, tapped.y + dx, tapped.x + dx, tapped.y - dx, markerPaint);
                }
            }
        }
    }
}
