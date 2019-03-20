# RICOH THETA Plug-in Slack Uploader Sample

撮った写真をSlackにアップロードする RICOH THETA プラグインです。

## 準備
* 投稿したい Slack ワークスペースで Bot を作成する
* API トークンを取得し、`UploadTask.java` の `SLACK_BOT_TOKEN` に設定する
```
static final private String SLACK_BOT_TOKEN = "write your slackbot token here";
```

* 投稿先チャネルIDを取得し、`MainActivity.java` の `channelIdList` に設定する

```
private List<String> channelIdList = Arrays.asList(
    "write your channel 1 ID here",
    "write your channel 2 ID here"
);
```

## 操作方法

| 操作                      | 機能                              |
| ------------------------- | --------------------------------- |
| シャッターボタン          | 静止画撮影後にSlackにアップロードする (CLモードで動作) |
| モードボタン              | 投稿するチャネルを変更する |
| モードボタン (長押し)     | プラグイン起動・終了              |

