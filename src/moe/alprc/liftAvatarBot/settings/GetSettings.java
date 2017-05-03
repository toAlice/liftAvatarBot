package moe.alprc.liftAvatarBot.settings;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alprc on 4/30/17.
 */
public class GetSettings {
    private static final int numberOfFiles = 1 << 6;

    public static final int EDIT_ROUNDED_CORNER = 1;
    public static final int EDIT_ROTATED = 1 << 1;
    public static final int EDIT_CIRCULAR = 1 << 2;
    public static final int EDIT_CROP = 1 << 3;
    public static final int EDIT_TRIM = 1 << 4;
    public static final int EDIT_SHADOW = 1 << 5;
    public static final int EDIT_HIGHER = 1 << 6;

    public static Settings getSettings(long chatId) {
        Settings settings = null;

        int fileNumber = toFileNumber(chatId);

        List<Settings> list = readChatSettingsList(fileNumber);

        for (Settings cs : list) {
            if (cs.getChatId() == chatId) {
                settings = cs;
                break;
            }
        }

        if (settings == null) {
            settings = new Settings(chatId);
            writeChatSettings(fileNumber, settings);
        }

        return settings;
    }

    public static Settings editSettings(long chatId, int editField, boolean value) {
        int fileNumber = toFileNumber(chatId);

        List<Settings> list = readChatSettingsList(fileNumber);
        Settings nSettings = null;

        for (Settings cs : list) {
            if (cs.getChatId() == chatId) {
                nSettings = getEdit(cs, editField, value);
                break;
            }
        }

        if (nSettings == null) {
            nSettings = new Settings(chatId);
            getEdit(nSettings, editField, value);
            list.add(nSettings);
        }

        writeChatSettingsList(fileNumber, list, false);

        return nSettings;
    }

    public static Settings editSettings(long chatId, int cropMethod) {
        int fileNumber = toFileNumber(chatId);

        List<Settings> list = readChatSettingsList(fileNumber);
        Settings nSettings = null;

        for (Settings cs : list) {
            if (cs.getChatId() == chatId) {
                nSettings = getEdit(cs, cropMethod);
                break;
            }
        }

        if (nSettings == null) {
            nSettings = new Settings(chatId);
            getEdit(nSettings, cropMethod);
            list.add(nSettings);
        }

        writeChatSettingsList(fileNumber, list, false);

        return nSettings;
    }

    public static void deleteSettings(long chatId) {
        int fileNumber = toFileNumber(chatId);
        List<Settings> list = readChatSettingsList(fileNumber);

        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getChatId() == chatId) {
                list.remove(i);
                found = true;
                break;
            }
        }

        if (found) {
            writeChatSettingsList(fileNumber, list, false);
        }
    }

    private static Settings getEdit(Settings cs, int keepArea) {
        cs.setKeepArea(keepArea);
        return cs;
    }

    private static Settings getEdit(Settings cs, int editField, boolean value) {
        switch (editField) {
            case EDIT_ROUNDED_CORNER:
                cs.setRoundedCorner(value);
                break;
            case EDIT_ROTATED:
                cs.setRotated(value);
                break;
            case EDIT_CIRCULAR:
                cs.setCircular(value);
                break;
            case EDIT_CROP:
                cs.setCrop(value);
                break;
            case EDIT_TRIM:
                cs.setTrim(value);
                break;
            case EDIT_SHADOW:
                cs.setShadow(value);
                break;
            case EDIT_HIGHER:
                cs.setHigher(value);
                break;
        }
        return cs;
    }

    private static int toFileNumber(long chatId) {
        return (int) (chatId % numberOfFiles);
    }

    private static String toPath(int fileNumber) {
        return String.format("users/settings%02d", fileNumber);
    }

    private static List<Settings> readChatSettingsList(int fileNumber) {
        File settingsFile = new File(toPath(fileNumber));

        ArrayList<Settings> list = new ArrayList<>();

        try (BufferedReader fin = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            while ((line = fin.readLine()) != null && line.length() > 0) {
                list.add(Settings.parseSettings(line));
            }
        } catch (IOException e) {
            //
        }

        return list;
    }

    private static void writeChatSettingsList(int fileNumber, List<Settings> list, boolean append) {
        File settingsFile = new File(toPath(fileNumber));

        try (BufferedWriter br = new BufferedWriter(new FileWriter(settingsFile, append))) {
            for (Settings settings : list) {
                String line = settings.toString();
                br.write(line);
                br.newLine();
            }
        } catch (IOException e) {
            //
        }
    }

    private static void writeChatSettings(int fileNumber, Settings settings) {
        File settingsFile = new File(toPath(fileNumber));

        try (BufferedWriter br = new BufferedWriter(new FileWriter(settingsFile, true))) {
            String line = settings.toString();
            br.write(line);
            br.newLine();
        } catch (IOException e) {
            //
        }
    }

    public static void main(String[] args) {
        long id = 56L;
        Settings settings = getSettings(id);
        System.out.println(settings.isRoundedCorner());
        System.out.println(settings.isRotated());
        System.out.println(settings.isCircular());
        System.out.println(settings.isCrop());
        System.out.println(settings.isTrim());
        System.out.println(settings.isShadow());
        System.out.println(settings.isHigher());
        System.out.println(settings.getKeepArea());
    }
}