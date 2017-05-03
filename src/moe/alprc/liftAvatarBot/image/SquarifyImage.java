package moe.alprc.liftAvatarBot.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by alprc on 4/30/17.
 */
public class SquarifyImage {
    public static final int KEEP_TOP_CENTER = 1;
    public static final int KEEP_BOTTOM_CENTER = 1 << 1;
    public static final int KEEP_CENTER_LEFT = 1 << 2;
    public static final int KEEP_CENTER_RIGHT = 1 << 3;
    public static final int KEEP_CENTER = 1 << 4;
    // to be implemented...
    public static final int KEEP_TOP_LEFT = 1 << 5;
    public static final int KEEP_TOP_RIGHT = 1 << 6;
    public static final int KEEP_BOTTOM_LEFT = 1 << 7;
    public static final int KEEP_BOTTOM_RIGHT = 1 << 8;

    static BufferedImage crop(BufferedImage image, int cropMethod) {
        int width = image.getWidth();
        int height = image.getHeight();

        int edge = width < height ? width : height;

        BufferedImage square;

        switch (cropMethod) {
            case KEEP_TOP_CENTER:
                square = image.getSubimage((width - edge) / 2, 0, edge, edge);
                break;
            case KEEP_CENTER_LEFT:
                square = image.getSubimage(0, (height - edge) / 2, edge, edge);
                break;
            case KEEP_BOTTOM_CENTER:
                square = image.getSubimage((width - edge) / 2, height - edge, edge, edge);
                break;
            case KEEP_CENTER_RIGHT:
                square = image.getSubimage(width - edge, (height - edge) / 2, edge, edge);
                break;
            case KEEP_CENTER:
                square = image.getSubimage((width - edge) / 2, (height - edge) / 2, edge, edge);
                break;
            default:
                square = null;
        }

        return square;
    }

    static BufferedImage notCrop(BufferedImage image, int keepPosition) {
        int width = image.getWidth();
        int height = image.getHeight();

        int edge = width > height ? width : height;

        BufferedImage square = new BufferedImage(edge, edge, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = square.createGraphics();

        switch (keepPosition) {
            case KEEP_TOP_CENTER:
                graphics.drawImage(image, (edge - width) / 2, 0, null);
                break;
            case KEEP_CENTER_RIGHT:
                graphics.drawImage(image, edge - width, (edge - height) / 2, null);
                break;
            case KEEP_BOTTOM_CENTER:
                graphics.drawImage(image, (edge - width) / 2, edge - height, null);
                break;
            case KEEP_CENTER_LEFT:
                graphics.drawImage(image, 0, (edge - height) / 2, null);
                break;
            case KEEP_CENTER:
                graphics.drawImage(image, (edge - width) / 2, (edge - height) / 2, null);
                break;
            default:
                square = image;
        }

        graphics.dispose();

        return square;
    }

    public static void main(String[] args) {
        int testCase = 2;

        switch (testCase) {
            case 0: for (int i = 0; i < 5; i++) {
                File overlay = new File("overlay.png");

                try {
                    BufferedImage image = ImageIO.read(overlay);

                    image = notCrop(image, KEEP_BOTTOM_CENTER);

                    ImageIO.write(image, "PNG", overlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
                break;
            case 1: {
                File overlay = new File("overlay.png");

                try {
                    BufferedImage image = ImageIO.read(overlay);

                    image = notCrop(image, KEEP_CENTER_RIGHT);

                    ImageIO.write(image, "PNG", overlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
