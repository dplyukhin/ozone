package choral.examples.ozone.modelserving;

import java.awt.Image;

public class ProcessedImages {
    private Image[] images;

    public ProcessedImages(Image[] images) {
        this.images = images;
    }

    public Image[] getImages() {
        return images;
    }

    public void addAll(ProcessedImages otherImages) {
        Image[] other = otherImages.getImages();
        Image[] newImages = new Image[images.length + other.length];
        System.arraycopy(images, 0, newImages, 0, images.length);
        System.arraycopy(other, 0, newImages, images.length, other.length);
        images = newImages;
    }

    public String toString() {
        return "ProcessedImages{" +
                "images=" + java.util.Arrays.toString(images) +
                '}';
    }
}
