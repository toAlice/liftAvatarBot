package moe.alprc.liftAvatarBot.image;

import moe.alprc.liftAvatarBot.settings.Settings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alprc on 2017/4/27/027.
 */
public class ImageProc {
    private static int npes = 0;

    private static final int OVERLAY_WIDTH = 256;
    private static final int OVERLAY_HEIGHT = 256;

    private static final double ROTATED_ROTATE_DEGREE = 34.5;
    private static final double NORMAL_ROTATE_DEGREE = -10.5;

    // ced
    private static final int OVERLAY_ROTATE_LOWER_OFFSET_X = 1;
    private static final int OVERLAY_ROTATE_LOWER_OFFSET_Y = -148;
    // dnc
    private static final int OVERLAY_ROTATE_HIGHER_OFFSET_X = 1;
    private static final int OVERLAY_ROTATE_HIGHER_OFFSET_Y = -148;

    // ced
    private static final int OVERLAY_NORMAL_HIGHER_OFFSET_X = -42;
    private static final int OVERLAY_NORMAL_HIGHER_OFFSET_Y = -140;
    // ced
    private static final int OVERLAY_NORMAL_LOWER_OFFSET_X = -38;
    private static final int OVERLAY_NORMAL_LOWER_OFFSET_Y = -120;

    private static final double ROUNDED_CORNER_RADIUS = 108.0;
    private static final double CIRCLE_CORNER_RADIUS = 256.0;

    private BufferedImage bufferedBase;
    private BufferedImage bufferedHand;
    private BufferedImage bufferedNormalRoundedRectangleHigher;
    private BufferedImage bufferedNormalRoundedRectangleLower;
    private BufferedImage bufferedNormalCircleHigher;
    private BufferedImage bufferedNormalCircleLower;
    private BufferedImage bufferedRotatedCircle;
    private BufferedImage bufferedRotatedRoundedRectangle;

    private int baseWidth;
    private int baseHeight;

    public ImageProc() {
        File base = new File("base.png");
        File hand = new File("hand.png");

        File normalRoundedRectangleHigher = new File("shadows/normalRoundedRectangleHigher.png");
        File normalRoundedRectangleLower = new File("shadows/normalRoundedRectangleLower.png");
        File normalCircleHigher = new File("shadows/normalCircleHigher.png");
        File normalCircleLower = new File("shadows/normalCircleLower.png");
        File rotatedRoundedRectangle = new File("shadows/rotatedRoundedRectangle.png");
        File rotatedCircle = new File("shadows/rotatedCircle.png");

        try {
            bufferedBase = ImageIO.read(base);
            bufferedHand = ImageIO.read(hand);

            bufferedNormalRoundedRectangleHigher = ImageIO.read(normalRoundedRectangleHigher);
            bufferedNormalRoundedRectangleLower = ImageIO.read(normalRoundedRectangleLower);
            bufferedNormalCircleHigher = ImageIO.read(normalCircleHigher);
            bufferedNormalCircleLower = ImageIO.read(normalCircleLower);
            bufferedRotatedRoundedRectangle = ImageIO.read(rotatedRoundedRectangle);
            bufferedRotatedCircle = ImageIO.read(rotatedCircle);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        baseWidth = bufferedBase.getWidth();
        baseHeight = bufferedBase.getHeight();
    }

    public byte[] hub(byte[] imageInByteArray, Settings settings) {
        int keepArea = settings.getKeepArea();

        boolean crop = settings.isCrop();
        boolean trim = settings.isTrim();
        boolean shadow = settings.isShadow();
        boolean higher = settings.isHigher();

        if (settings.isCircular()) {
            if (settings.isRotated()) {
                imageInByteArray = rotatedCircleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
            } else {
                imageInByteArray = normalCircleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
            }
        } else {
            if (settings.isRoundedCorner()) {
                if (settings.isRotated()) {
                    imageInByteArray = rotatedRoundedRectangleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
                } else {
                    imageInByteArray = normalRoundedRectangleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
                }
            } else {
                if (settings.isRotated()) {
                    imageInByteArray = rotatedRectangleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
                } else {
                    imageInByteArray = normalRectangleCombine(imageInByteArray, keepArea, crop, trim, shadow, higher);
                }
            }
        }

        return imageInByteArray;
    }

    public byte[] rotatedCircleCombine(byte[] overlay, int keepArea,
                                     boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, CIRCLE_CORNER_RADIUS, true, isCrop, trim, shadow, higher);
    }

    public byte[] rotatedRectangleCombine(byte[] overlay, int keepArea,
                                        boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, 0, true, isCrop, trim, shadow, higher);
    }

    public byte[] rotatedRoundedRectangleCombine(byte[] overlay, int keepArea,
                                               boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, ROUNDED_CORNER_RADIUS, true, isCrop, trim, shadow, higher);
    }

    public byte[] normalCircleCombine(byte[] overlay, int keepArea,
                                    boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, CIRCLE_CORNER_RADIUS, false, isCrop, trim, shadow, higher);
    }

    public byte[] normalRectangleCombine(byte[] overlay, int keepArea,
                                       boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, 0, false, isCrop, trim, shadow, higher);
    }

    public byte[] normalRoundedRectangleCombine(byte[] overlay, int keepArea,
                                              boolean isCrop, boolean trim, boolean shadow, boolean higher) {
        return combine(overlay, keepArea, ROUNDED_CORNER_RADIUS, false, isCrop, trim, shadow, higher);
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        Image temp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage nImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = nImage.createGraphics();
        graphics.drawImage(temp, 0, 0, null);
        graphics.dispose();

        return nImage;
    }

    private static BufferedImage rotate(BufferedImage bufferedOverlay, double rotateDegree) {
        double rate = (1.0 + Math.sqrt(3.0)) / 2.0;

        BufferedImage base = new BufferedImage((int) Math.ceil(OVERLAY_WIDTH * rate),
                (int) Math.ceil(OVERLAY_HEIGHT * rate), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = base.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(bufferedOverlay, (int) (OVERLAY_WIDTH * (rate - 1)),
                (int) (OVERLAY_WIDTH * (rate - 1)), OVERLAY_WIDTH, OVERLAY_HEIGHT, null);

        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotateDegree),
                base.getWidth() / 2, base.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        graphics.dispose();

        return op.filter(base, null);
    }

    private static BufferedImage toRounded(BufferedImage bufferedOverlay, double cornerRadius) {
        BufferedImage rounded =
                new BufferedImage(OVERLAY_WIDTH, OVERLAY_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = rounded.createGraphics();

        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.white);

        graphics.fill(new RoundRectangle2D.Double(0, 0, OVERLAY_WIDTH, OVERLAY_HEIGHT,
                cornerRadius, cornerRadius));

        graphics.setComposite(AlphaComposite.SrcAtop);
        graphics.drawImage(bufferedOverlay, 0, 0, null);

        graphics.dispose();

        return rounded;
    }

    private byte[] combine(byte[] input, int keepPosition, double cornerRadius,
                                 boolean rotated, boolean crop, boolean trim, boolean shadow, boolean higher) {
        BufferedImage bufferedOverlay;

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(input))) {
            bufferedOverlay = ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (trim) {
            bufferedOverlay = trim(bufferedOverlay);
        }

        if (crop) {
            bufferedOverlay = SquarifyImage.crop(bufferedOverlay, keepPosition);
        } else {
            bufferedOverlay = SquarifyImage.notCrop(bufferedOverlay, keepPosition);
        }

        bufferedOverlay = resize(bufferedOverlay, OVERLAY_WIDTH, OVERLAY_HEIGHT);

        if (cornerRadius != 0.0) {
            bufferedOverlay = toRounded(bufferedOverlay, cornerRadius);
        }

        if (rotated) {
            bufferedOverlay = rotate(bufferedOverlay, ROTATED_ROTATE_DEGREE);
            if (higher) {
                bufferedOverlay = combine(bufferedBase, bufferedOverlay,
                        OVERLAY_ROTATE_HIGHER_OFFSET_X, OVERLAY_ROTATE_HIGHER_OFFSET_Y);
            } else {
                bufferedOverlay = combine(bufferedBase, bufferedOverlay,
                        OVERLAY_ROTATE_LOWER_OFFSET_X, OVERLAY_ROTATE_LOWER_OFFSET_Y);
            }
        } else {
            bufferedOverlay = rotate(bufferedOverlay, NORMAL_ROTATE_DEGREE);
            if (higher) {
                bufferedOverlay = combine(bufferedBase, bufferedOverlay,
                        OVERLAY_NORMAL_HIGHER_OFFSET_X, OVERLAY_NORMAL_HIGHER_OFFSET_Y);
            } else {
                bufferedOverlay = combine(bufferedBase, bufferedOverlay,
                        OVERLAY_NORMAL_LOWER_OFFSET_X, OVERLAY_NORMAL_LOWER_OFFSET_Y);
            }
        }

        if (shadow) {
            bufferedOverlay = combine(bufferedOverlay, getShadow(cornerRadius, rotated, higher), 0, 0);
        }

        bufferedOverlay = combine(bufferedOverlay, bufferedHand, 0, 0);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedOverlay, "webp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }

    private BufferedImage getShadow(double cornerRadius, boolean rotated, boolean higher) {
        BufferedImage shadow;
        if (cornerRadius == CIRCLE_CORNER_RADIUS) {
            if (rotated) {
                shadow = bufferedRotatedCircle;
            } else {
                if (higher) {
                    shadow = bufferedNormalCircleHigher;
                } else {
                    shadow = bufferedNormalCircleLower;
                }
            }
        } else if (cornerRadius == ROUNDED_CORNER_RADIUS) {
            if (rotated) {
                shadow = bufferedRotatedRoundedRectangle;
            } else {
                if (higher) {
                    shadow = bufferedNormalRoundedRectangleHigher;
                } else {
                    shadow = bufferedNormalRoundedRectangleLower;
                }
            }
        } else {
            shadow = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);
        }
        return shadow;
    }

    private BufferedImage combine(BufferedImage bufferedBase, BufferedImage bufferedOverlay, int x, int y) {
        BufferedImage combined = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = combined.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(bufferedBase, 0, 0, null);
        graphics.drawImage(bufferedOverlay, x, y, null);

        graphics.dispose();

        return combined;
    }

    private static BufferedImage trim(BufferedImage image) {
        WritableRaster raster = image.getAlphaRaster();

        if (raster != null) {
            int width = raster.getWidth();
            int height = raster.getHeight();

            int top = 0;
            int left = 0;
            int right = width - 1;
            int bottom = height - 1;
            int minRight = width - 1;
            int minBottom = height - 1;

            top:
            for (; top < bottom; top++) {
                for (int x = 0; x < width; x++) {
                    if (raster.getSample(x, top, 0) != 0) {
                        minRight = x;
                        minBottom = top;
                        break top;
                    }
                }
            }

            left:
            for (; left < minRight; left++) {
                for (int y = height - 1; y > top; y--) {
                    if (raster.getSample(left, y, 0) != 0) {
                        minBottom = y;
                        break left;
                    }
                }
            }

            bottom:
            for (; bottom > minBottom; bottom--) {
                for (int x = width - 1; x > left; x--) {
                    if (raster.getSample(x, bottom, 0) != 0) {
                        minRight = x;
                        break bottom;
                    }
                }
            }

            right:
            for (; right > minRight; right--) {
                for (int y = bottom; y >= top; y--) {
                    if (raster.getSample(right, y, 0) != 0) {
                        break right;
                    }
                }
            }

            return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
        } else {
            return image;
        }
    }

    public static void main(String[] args) {
        ImageProc imageProc = new ImageProc();

        int testCase = 1;

        switch (testCase) {
            case 0:
                for (int i = 0; i < 5; i++) {
                    try {
                        Path p = Paths.get("overlay.png");

                        byte[] output = imageProc.normalRectangleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_TOP_CENTER,
                                false, true, true, true);

                        Files.write(p, output);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1: {
                try {
                    Path p = Paths.get("overlay.png");

                    // byte[] output = imageProc.normalRectangleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_BOTTOM_CENTER, false, true, true, true);
                    //
                    // byte[] output = imageProc.rotatedRoundedRectangleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, true, true, true, true);
                    // byte[] output = imageProc.rotatedCircleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, true, true, true, true);
                    // byte[] output = imageProc.normalRoundedRectangleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, true, true, true, true);
                    // byte[] output = imageProc.normalRoundedRectangleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, true, true, true, false);
                    // byte[] output = imageProc.normalCircleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, false, true, true, true);
                    byte[] output = imageProc.normalCircleCombine(Files.readAllBytes(p), SquarifyImage.KEEP_CENTER, true, true, true, false);

                    Files.write(p, output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case 2: {
                File rotate = new File("rotate.png");
                try {
                    BufferedImage image = ImageIO.read(rotate);

                    image = ImageProc.rotate(image, ROTATED_ROTATE_DEGREE);

                    ImageIO.write(image, "PNG", rotate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case 3: {
                File rounded = new File("rounded.png");
                try {
                    BufferedImage image = ImageIO.read(rounded);

                    image = ImageProc.toRounded(image, ROUNDED_CORNER_RADIUS);

                    ImageIO.write(image, "PNG", rounded);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case 4: {
                File cr = new File("cr.png");
                try {
                    BufferedImage image = ImageIO.read(cr);

                    image = SquarifyImage.crop(image, SquarifyImage.KEEP_CENTER);
                    image = ImageProc.resize(image, 512, 512);

                    ImageIO.write(image, "PNG", cr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case 5: {
                File overlay = new File("overlay.png");

                try {
                    BufferedImage image = ImageIO.read(overlay);

                    image = trim(image);

                    ImageIO.write(image, "PNG", overlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
