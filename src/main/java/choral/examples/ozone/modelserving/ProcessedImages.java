package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class ProcessedImages implements Serializable {
    private Image[] images;
    private int[] imgIDs;

    public ProcessedImages(Image[] images, int[] imgIDs) {
        this.images = images;
        this.imgIDs = imgIDs;
    }

    public Image[] getImages() {
        return images;
    }

    public int[] getImgIDs() {
        return imgIDs;
    }

    public void addAll(ProcessedImages otherImages) {
        Image[] other = otherImages.getImages();
        Image[] newImages = new Image[images.length + other.length];
        System.arraycopy(images, 0, newImages, 0, images.length);
        System.arraycopy(other, 0, newImages, images.length, other.length);

        int[] otherIDs = otherImages.getImgIDs();
        int[] newIDs = new int[imgIDs.length + otherIDs.length];
        System.arraycopy(imgIDs, 0, newIDs, 0, imgIDs.length);
        System.arraycopy(otherIDs, 0, newIDs, imgIDs.length, otherIDs.length);

        images = newImages;
    }

    public String toString() {
        return "ProcessedImages{" +
                "images=" + java.util.Arrays.toString(images) +
                ", imgIDs=" + java.util.Arrays.toString(imgIDs) +
                '}';
    }
}
