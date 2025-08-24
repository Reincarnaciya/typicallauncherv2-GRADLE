package space.typro.Download;

public interface DownloadListener {
    default void onDownloadStart(DownloadTask task) {};
    default void onProgressUpdate(DownloadTask task, int progress) {};
    default void onDownloadComplete(DownloadTask task) {};
    default void onDownloadError(DownloadTask task, Exception e) {e.printStackTrace();};
    default void onAllDownloadsCompleted() {};
}