package xyz.vadimszzz.okhttplogger;

import java.io.FileOutputStream;
import java.sql.Timestamp;

import android.app.Application;
import android.content.Context;

import static xdroid.core.Global.getContext;
import static xdroid.core.ObjectUtils.notNull;


/* Internal storage file logger. */
public class FileLogger {
    private String filename;
    private FileOutputStream fos;

    public FileLogger(String filename) {
        this.filename = filename;
    }

    /**
     * Open file on first call.
     */
    private void initialize() {
        if (this.fos == null) {
            try {
                Application app = (Application) notNull(getContext());
                this.fos = app.openFileOutput(this.filename, Context.MODE_APPEND);
            } catch(Exception e) {
                OkHttpLogger.getXposedLogger().log("failed to start logger: %s", e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    FileLogger.this.finalize(); // this$0
                }
            });
        }
    }

    /**
     * Close file on shutdown.
     */
    public void finalize() {
        try {
            this.fos.flush();
            this.fos.close();
        } catch(Exception e) {
        }
    }

    /**
     * Append to log file in internal storage of the hooked app.
     * 
     * @param String message        Message will be formatted if additional arguments passed.
     * @param Object[] objects      
     */
    public void log(String message, Object ... objects) {
        if (objects.length > 0) {
            message = String.format(message, (Object[]) objects);
        }
        try {
            initialize();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.fos.write(String.format("%-31s %s\n", timestamp, message).getBytes());
            this.fos.flush();
        } catch(Exception e) {
            OkHttpLogger.getXposedLogger().log("failed to log request URL: " + e);
        }
    }
}
