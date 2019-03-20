/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.slackuploader;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import com.theta360.slackuploader.task.TakePictureTask;
import com.theta360.slackuploader.task.TakePictureTask.Callback;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.slackuploader.task.UploadTask;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends PluginActivity {

    static final private String TAG = MainActivity.class.getSimpleName();
    private Iterator<String> iterator;
    private String mCurrentChannel;
    private List<String> channelIdList = Arrays.asList(
            "write your channel 1 ID here",
            "write your channel 2 ID here"
    );

    private TakePictureTask.Callback mTakePictureTaskCallback = new Callback() {
        @Override
        public void onTakePicture(String fileUrl) {
            String filePath = resolveFilePath(fileUrl);
            if (filePath != null) {
                new UploadTask(mUploadTaskCallback, mCurrentChannel, filePath).execute();
            }
        }
    };
    private UploadTask.Callback mUploadTaskCallback = new UploadTask.Callback() {
        @Override
        public void onSuccess(String message) {
            notificationAudioClose();
            Log.d(TAG, "Succeed to upload: " + message);
        }
        @Override
        public void onError(String message) {
            Log.d(TAG, "Fail to upload: " + message);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set enable to close by pluginlibrary, If you set false, please call close() after finishing your end processing.
        setAutoClose(true);
        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    /*
                     * To take a static picture, use the takePicture method.
                     * You can receive a fileUrl of the static picture in the callback.
                     */
                    new TakePictureTask(mTakePictureTaskCallback).execute();
                } else if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    if (!iterator.hasNext()) {
                        iterator = channelIdList.iterator();
                    }
                    mCurrentChannel = iterator.next();
                    Log.d(TAG, "Set current channel to " + mCurrentChannel);
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
                /**
                 * You can control the LED of the camera.
                 * It is possible to change the way of lighting, the cycle of blinking, the color of light emission.
                 * Light emitting color can be changed only LED3.
                 */
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {

            }
        });

        iterator = channelIdList.iterator();
        this.mCurrentChannel = iterator.next();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isApConnected()) {

        }
    }

    @Override
    protected void onPause() {
        // Do end processing
        //close();

        super.onPause();
    }

    private String resolveFilePath(String fileUrl) {
        // Example
        //  * fileUrl:  http://192.168.1.1/files/150100525831424d42075975ce2ec300/100RICOH/R0010256.JPG
        //  * filePath: /storage/emulated/0/DCIM/100RICOH/R0010256.JPG

        if (fileUrl == null) {
            Log.e(TAG, "Fail URL is null");
            return null;
        }

        String[] splitFilelUrl = fileUrl.split("/", 0);
        if (splitFilelUrl.length < 7) {
            Log.e(TAG, "Invalid file URL");
            return null;
        }

        String DCF = splitFilelUrl[5];
        if (!DCF.matches("^[0-9]{3}[A-Z]{5}$")) {
            Log.e(TAG, "Invalid DCF directory");
            return null;
        }

        String fileName = splitFilelUrl[6];
        if (!fileName.matches(".*[jpg|JPG]$")) {
            Log.e(TAG, "Invalid file name");
            return null;
        }

        final String DCIM = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();

        String filePath = DCIM + "/" + DCF + "/" + fileName;
        Log.d(TAG, "filePath: " + filePath);
        return filePath;
    }
}
