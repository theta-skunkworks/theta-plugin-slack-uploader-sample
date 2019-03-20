package com.theta360.slackuploader.task;


import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;



public class UploadTask extends AsyncTask<Void, Void, String> {

    static final private String TAG = UploadTask.class.getSimpleName();
    static final private String API_URL = "https://slack.com/api/files.upload";
    static final private String SLACK_BOT_TOKEN = "write your slackbot token here";

    private final Callback callback;
    private final String channel;
    private final String filePath;
    private final OkHttpClient client;

    public UploadTask(Callback callback, String channel, String filePath) {

        this.callback = Objects.requireNonNull(callback, "callback can not be null.");
        this.channel = Objects.requireNonNull(channel, "channel can not be null.");
        this.filePath = Objects.requireNonNull(filePath, "file path can not be null.");

        // Build HTTP client
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    private String postToSlackbot() {

        // Get image file
        Log.d(TAG, "path: " + this.filePath);
        File file = new File(this.filePath);
        if (!file.exists()) {
            Log.d(TAG, "Fail to read file");
            return "";
        }
        Log.d(TAG, "Succeed to read file");

        // Build request body
        final String timestamp = getDateString();
        final String boundary = String.valueOf(System.currentTimeMillis());
        RequestBody requestBody = new MultipartBody.Builder(boundary)
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        filePath,
                        RequestBody.create(MediaType.parse("image/jpeg"), file)
                )
                .addFormDataPart("token", SLACK_BOT_TOKEN)
                .addFormDataPart("channels", this.channel)
                .addFormDataPart("filename", "IMG_" + timestamp + ".jpg")
                .addFormDataPart("filetype", "jpg")
                .addFormDataPart("initial_comment", "THETA V SlackUploaderから画像がアップロードされました。")
                .addFormDataPart("title", "IMG_" + timestamp + ".jpg")
                .build();

        // Build HTTP request
        Request request = new Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .build();

        Call call = client.newCall(request);
        String result = null;

        try {
            Response response = call.execute();
            ResponseBody body = response.body();
            if (body != null) {
                result = body.string();
            } else {
                Log.e(TAG, "Response body is null");
            }
        } catch (IOException e) {
            Log.d(TAG, "Result: " + e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "Result: " + result);
        return result;
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = postToSlackbot();
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            callback.onSuccess(result);
        } else {
            callback.onError("An error is occurred.");
        }
    }

    private String getDateString() {
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_kkmmss");
        return sdf.format(cl.getTime());
    }

    public interface Callback {
        void onSuccess(String message);
        void onError(String message);
    }
}

