package choral.examples.ozone.modelserving;


public class Image {
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
