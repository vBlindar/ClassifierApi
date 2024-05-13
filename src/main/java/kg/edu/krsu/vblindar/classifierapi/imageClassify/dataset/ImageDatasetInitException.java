package kg.edu.krsu.vblindar.classifierapi.imageClassify.dataset;


public class ImageDatasetInitException extends Exception {
    public ImageDatasetInitException() {

    }

    public ImageDatasetInitException(String message) {
        super(message);
    }

    public ImageDatasetInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageDatasetInitException(Throwable cause) {
        super(cause);
    }

    public ImageDatasetInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}