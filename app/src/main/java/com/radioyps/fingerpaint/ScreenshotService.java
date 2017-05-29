package com.radioyps.fingerpaint;

/**
 * Created by yep on 28/05/17.
 */

import android.app.*;
import android.content.*;
import android.widget.*;
import android.util.*;
import android.webkit.*;
import android.graphics.*;
import java.io.*;
import android.view.View.*;
import android.os.*;
import android.os.Process;

public class ScreenshotService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Message msg;
    private static final String url_weather = "http://weather.gc.ca/wxlink/wxlink.html?cityCode=qc-147&amp;lang=e";

    private WebView webview;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            webview = new WebView(ScreenshotService.this);
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);

            //without this toast message, screenshot will be blank, dont ask me why...
            Toast.makeText(ScreenshotService.this, "Taking screenshot...", Toast.LENGTH_SHORT).show();


            // This is the important code :)
            webview.setDrawingCacheEnabled(true);

            //width x height of your webview and the resulting screenshot
            webview.measure(600, 400);
            webview.layout(0, 0, 600, 400);


            webview.loadUrl(url_weather);

            webview.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    //without this method, your app may crash...
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    //new takeScreenshotTask().execute();
                    takeScreeshot();
                    stopSelf();


                }
            });


        }
    }

    private void takeScreeshot(){
        synchronized (this) {try {wait(20350);} catch (InterruptedException e) {}}

        //here I save the bitmap to file
        Bitmap b = webview.getDrawingCache();

        //File file = new File("example-screenshot.png");
        File file = new File(ScreenshotService.this.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), "example-screenshot.png");
        OutputStream out;


        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

        } catch (IOException e) {
            Log.e("ScreenshotService", "IOException while trying to save thumbnail, Is /sdcard/ writable?");

            e.printStackTrace();
        }

        Toast.makeText(ScreenshotService.this, "Screenshot taken", Toast.LENGTH_SHORT).show();
    }


    private class takeScreenshotTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void[] p1) {

            //allow the webview to render
            synchronized (this) {try {wait(350);} catch (InterruptedException e) {}}

            //here I save the bitmap to file
            Bitmap b = webview.getDrawingCache();

            //File file = new File("example-screenshot.png");
            File file = new File(ScreenshotService.this.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), "example-screenshot.png");
            OutputStream out;


            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

            } catch (IOException e) {
                Log.e("ScreenshotService", "IOException while trying to save thumbnail, Is /sdcard/ writable?");

                e.printStackTrace();
            }

            Toast.makeText(ScreenshotService.this, "Screenshot taken", Toast.LENGTH_SHORT).show();




            return null;
        }
    }

//service related stuff below, its probably easyer to use intentService...

    @Override
    public void onCreate() {

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {

    }


}
/*
shareimprove this answer
edited Apr 1 '15 at 18:02
answered Jan 30 '15 at 11:18

JonasCz
7,44252150

how do you start this service and call it? – Shimon Doodkin Sep 9 '16 at 6:27
1
@ShimonDoodkin Something like this: Intent intent = new Intent(this, SaveService.class); intent.putExtra(Intent.EXTRA_TEXT, "http://somewebsite.com/somepage");. Also note that there's a better / newer version of this code here: github.com/JonasCz/save-for-offline/blob/master/app/src/main‌​/… – JonasCz Sep 9 '16 at 7:09

at first it did not work , then this did: onCreate() { ... mServiceHandler = new ServiceHandler(Looper.getMainLooper()); – Shimon Doodkin Sep 11 '16 at 22:45
add a comment
Your Answer
*/

