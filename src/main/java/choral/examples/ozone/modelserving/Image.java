package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class Image implements Serializable {
    private byte[] imgBytes;

    public Image(byte[] imgBytes) {
        this.imgBytes = imgBytes;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }

    public String toString() {
        return "Image{" +
                "imgBytes=" + java.util.Arrays.toString(imgBytes) +
                '}';
    }
}
