package moe.alprc.liftAvatarBot.settings;

import moe.alprc.liftAvatarBot.image.SquarifyImage;
/**
 * Created by alprc on 4/30/17.
 */
public class Settings {
    private long chatId;

    private boolean roundedCorner = false;
    private boolean rotated = false;
    private boolean circular = true;
    private boolean crop = false;
    private boolean trim = true;
    private boolean shadow = true;
    private boolean higher = false;

    private int keepArea = SquarifyImage.KEEP_CENTER;

    Settings(long chatId) {
        this.chatId = chatId;
    }

    Settings(long chatId, boolean roundedCorner, boolean rotated, boolean circular,
             boolean crop, boolean trim, boolean shadow, boolean higher, int keepArea) {
        this.chatId = chatId;
        this.roundedCorner = roundedCorner;
        this.rotated = rotated;
        this.circular = circular;
        this.crop = crop;
        this.trim = trim;
        this.shadow = shadow;
        this.higher = higher;
        this.keepArea = keepArea;
    }

    public boolean isRoundedCorner() {
        return roundedCorner;
    }

    public long getChatId() {
        return chatId;
    }

    private void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setRoundedCorner(boolean roundedCorner) {
        this.roundedCorner = roundedCorner;
    }

    public boolean isRotated() {
        return rotated;
    }

    public void setRotated(boolean rotated) {
        this.rotated = rotated;
    }

    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isHigher() {
        return higher;
    }

    public void setHigher(boolean lower) {
        this.higher = lower;
    }

    public int getKeepArea() {
        return keepArea;
    }

    public void setKeepArea(int keepArea) {
        this.keepArea = keepArea;
    }

    @Override
    public String toString() {
        return chatId +
                (roundedCorner ? " t " : " f ") +
                (rotated ? "t " : "f ") +
                (circular ? "t " : "f ") +
                (crop ? "t " : "f ") +
                (trim ? "t " : "f ") +
                (shadow ? "t " : "f ") +
                (higher ? "t " : "f ") +
                keepArea;
    }

    public static Settings parseSettings(String line) {
        String[] parts = line.split(" ");

        int index = 0;
        long chatId = Long.parseLong(parts[index]);
        boolean roundedCorner = parts[++index].equals("t");
        boolean rotated = parts[++index].equals("t");
        boolean circular = parts[++index].equals("t");
        boolean crop = parts[++index].equals("t");
        boolean trim = parts[++index].equals("t");
        boolean shadow = parts[++index].equals("t");
        boolean higher = parts[++index].equals("t");
        int keepArea = Integer.parseInt(parts[++index]);

        return new Settings(chatId, roundedCorner, rotated, circular, crop, trim, shadow, higher, keepArea);
    }
}

