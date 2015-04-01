package hoverpuck.anomalousmaker.com.hoverpuckcontroller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import android.os.Handler;

public class HoverPuck {
    private final int CENTER = 0;
    private final int LEFT   = 1;
    private final int RIGHT  = 2;
    private final int SERVO  = 3;

    private OutputStream btOutStream;
    private Handler cmdQueueHandler;
    private Runnable cmdQueueWorker;

    private DecimalFormat floatFormat;

    private String lastCmd[];


    public HoverPuck(OutputStream os)
    {
        // Init Bluetooth OutputStream
        btOutStream = os;

        lastCmd = new String[4];

        floatFormat = new DecimalFormat("0.00");
    }

    public void write(String str)
    {
        try {
            btOutStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();

            // TODO: Bluetooth has disconnected
        }
    }

    public void disableAll()
    {
        // Disable EDFs + Servo
        write("s0d;");
        write("s1d;");
        write("s2d;");
        write("s3d;");

        cmdQueueHandler.removeCallbacks(cmdQueueWorker);
    }

    private void setEDF(float value, int edf)
    {
        String cmd;

        // Workaround:
        //  Value ranges from 0-1.
        //  Math it such that it ranges from 0.25 to 0.7
        value = value * 0.45f + 0.25f;
        cmd = "s" + String.valueOf(edf) + "=" + floatFormat.format(value) + ";";

        if (cmd != lastCmd[edf])
        {
            write(cmd);
            lastCmd[edf] = cmd;
        }
    }

    public void setLeft(float value)
    {
        setEDF(value, LEFT);
    }

    public void setRight(float value)
    {
        setEDF(value, RIGHT);
    }

    public int getLipoPercentage()
    {
        // TODO: Implement
        return 100;
    }

    public int getRSSI()
    {
        // TODO: Implement
        return -40;
    }
}
