package hoverpuck.anomalousmaker.com.hoverpuckcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.support.v4.view.MotionEventCompat;
import android.view.View;


public class ControllerUI extends View {
    private static final int holo = Color.parseColor("#FF33B5E5");
    public static boolean btConnected = false;

    public HoverPuck robot;

    // Painting Styles
    private static Paint lines;	// Lines Style
    private static Paint fill;	// Fill Style
    private static Paint text;	// Text Style

    // UI Elements
    private static Rect left_bounds;
    private static Rect left_fill;
    private static Rect right_bounds;
    private static Rect right_fill;

    // Invisible UI Contact Areas
    private static Rect left_contact_area;
    private static Rect right_contact_area;

    // Text Indicators
    public static String status = "Disconnected";
    private static String left_percentage = "---";
    private static String right_percentage = "---";
    private static String rssi_percentage = "---";
    private static String lipo_percentage = "---";


    public ControllerUI(Context context) {
        super(context);

        initUI();
    }

    public ControllerUI(Context context, AttributeSet attrs) {
        super(context, attrs);

        initUI();
    }

    public ControllerUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initUI();
    }


    private void initUI() {
        // Init Styles
        lines = new Paint();
        lines.setColor(holo);
        lines.setStrokeWidth(3.0f);
        lines.setStyle(Style.STROKE);

        fill = new Paint();
        fill.setColor(holo);
        fill.setAlpha((int) (255*0.3f));
        fill.setStyle(Style.FILL);

        text = new Paint();
        text.setColor(Color.WHITE);


        // UI Objects
        left_bounds = new Rect();
        left_fill = new Rect();
        right_bounds = new Rect();
        right_fill = new Rect();

        // UI Contact Areas
        left_contact_area = new Rect();
        right_contact_area = new Rect();


        // Background Color
        setBackgroundColor(Color.BLACK);

        robot = new HoverPuck(null);
    }

    public void setStatus(String str)
    {
        status = str;

        invalidate();
    }

    public void enableUI(boolean connected)
    {
        btConnected = connected;

        if (connected){
            left_percentage = "0";
            right_percentage = "0";
            rssi_percentage = String.valueOf(robot.getRSSI());
            lipo_percentage = String.valueOf(robot.getLipoPercentage());
        } else {
            left_percentage = "---";
            right_percentage = "---";
            rssi_percentage = "---";
            lipo_percentage = "---";

            // Set fills to 0%
            left_fill.set(left_bounds.left, left_bounds.bottom, left_bounds.right, left_bounds.bottom);
            right_fill.set(right_bounds.left, right_bounds.bottom, right_bounds.right, right_bounds.bottom);
        }

        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Swap variables if needed
        if (width < height)
        {
            int tmp = width;
            width = height;
            height = tmp;
        }


        // Update Interactive UI
        drawRect(left_bounds, width*0.25f, (height-20)/2.0f, (width-20)*0.2f, (height-20)*0.75f);
        drawRect(right_bounds, width*0.75f, (height-20)/2.0f, (width-20)*0.2f, (height-20)*0.75f);

        //  Redraw contact area here since _width_ and _height_ are determined on screen redraw
        drawRect(left_contact_area, width*0.25f, (height-20)/2.0f, (width-20)*(0.25f + 0.3f), (height-20)*(0.75f+ + 0.3f));
        drawRect(right_contact_area, width*0.75f, (height-20)/2.0f, (width-20)*(0.25f + 0.3f), (height-20)*(0.75f+ + 0.3f));

        canvas.drawRect(left_bounds, lines);
        canvas.drawRect(right_bounds, lines);
        canvas.drawRect(left_fill, fill);
        canvas.drawRect(right_fill, fill);


        // Update indicators
        //  Status
        text.setTextSize(height * 0.05f);
        text.setTextAlign(Align.LEFT);
        canvas.drawText(status, 10, height-10, text);

        //  RSSI
        text.setTextAlign(Align.CENTER);
        canvas.drawText(String.valueOf(robot.getRSSI()) + " dBm", width, height-40, text);

        //  Lipo
        text.setTextAlign(Align.CENTER);
        canvas.drawText(String.valueOf(robot.getLipoPercentage()) + "%", width, height-40, text);

        //  Powers
        text.setTextAlign(Align.CENTER);
        canvas.drawText(left_percentage + "%", left_bounds.centerX(), left_bounds.top-12, text);
        text.setTextAlign(Align.CENTER);
        canvas.drawText(right_percentage + "%", right_bounds.centerX(), right_bounds.top-12, text);

    }

    private static void drawRect(Rect rect, float x, float y, float w, float h)
    {
        rect.set((int) (x-(w/2)), (int) (y-(h/2)), (int) (x+(w/2)), (int) (y+(h/2)));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (btConnected) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                // Cycle through all active points since onTouchEvent only generates an ACTION_MOVE for the first point...
                for (int i = 0; i < event.getPointerCount(); i++) {

                    int x = (int) MotionEventCompat.getX(event, i);
                    int y = (int) MotionEventCompat.getY(event, i);

                    // Left Control
                    if (left_contact_area.contains(x, y)) {
                        // Inside of hover hit area
                        if (y < left_bounds.top) {
                            left_fill.set(left_bounds.left, left_bounds.top, left_bounds.right, left_bounds.bottom);
                            left_percentage = "100";
                        } else if (y > left_bounds.bottom) {
                            left_fill.set(left_bounds.left, left_bounds.bottom, left_bounds.right, left_bounds.bottom);
                            left_percentage = "0";
                        } else {
                            left_fill.set(left_bounds.left, y, left_bounds.right, left_bounds.bottom);
                            left_percentage = String.valueOf((int) (100 * (left_bounds.height() - (y - left_bounds.top))) / left_bounds.height());
                        }
                    }

                    // Right Control
                    if (right_contact_area.contains(x, y)) {
                        if (y < right_bounds.top) {
                            right_fill.set(right_bounds.left, right_bounds.top, right_bounds.right, right_bounds.bottom);
                            right_percentage = "100";
                        } else if (y > right_bounds.bottom) {
                            right_fill.set(right_bounds.left, right_bounds.bottom, right_bounds.right, right_bounds.bottom);
                            right_percentage = "0";
                        } else {
                            right_fill.set(right_bounds.left, y, right_bounds.right, right_bounds.bottom);
                            right_percentage = String.valueOf((int) (100 * (right_bounds.height() - (y - right_bounds.top))) / right_bounds.height());
                        }
                    }
                }
            }

            // Update robot
            robot.setLeft(Integer.parseInt(left_percentage)/100.0f);
            robot.setRight(Integer.parseInt(right_percentage)/100.0f);

            // Request to redraw screen
            invalidate();
        }

        return true;
    }

}
