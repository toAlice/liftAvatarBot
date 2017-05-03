package moe.alprc.liftAvatarBot.image;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;

/**
 * Created by alprc on 2017/4/29/029.
 */
public class Rectangle {
    private static class IntPair {
        int a;
        int b;

        IntPair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    private static IntPair getSize(File image) {
        int width, height;
        try {
            BufferedImage bufferedImage = ImageIO.read(image);
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new IntPair(width, height);
    }

    public static boolean isSquare(File image) {
        IntPair intPair = getSize(image);
        if (intPair == null) {
            return false;
        }
        return intPair.a == intPair.b;
    }

    public static boolean isHorizontal(File image) {
        IntPair intPair = getSize(image);
        if (intPair == null) {
            return false;
        }
        return intPair.a >= intPair.b;
    }

    public static boolean isVertical(File image) {
        IntPair intPair = getSize(image);
        if (intPair == null) {
            return false;
        }
        return intPair.a <= intPair.b;
    }
}
