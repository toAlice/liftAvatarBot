package moe.alprc.liftAvatarBot;

import moe.alprc.liftAvatarBot.image.ImageProc;
import moe.alprc.liftAvatarBot.image.SquarifyImage;
import moe.alprc.liftAvatarBot.settings.GetSettings;
import moe.alprc.liftAvatarBot.settings.Settings;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static moe.alprc.liftAvatarBot.settings.GetSettings.deleteSettings;
import static moe.alprc.liftAvatarBot.settings.GetSettings.editSettings;

/**
 * Created by alprc on 2017/4/27/027.
 */
public class LiftAvatarBot extends TelegramLongPollingBot {
    private static ImageProc imageProc;
    private static String helpText;
    private static String token;

    // commands
    private static final String ROUNDED_CORNER = "roundedcorner";
    private static final String ROTATED = "rotated";
    private static final String CIRCULAR = "circular";
    private static final String CROP = "crop";
    private static final String TRIM = "trim";
    private static final String SHADOW = "shadow";
    private static final String HIGHER = "higher";
    private static final String KEEP_AREA = "keeparea";
    private static final String RESET = "reset";
    private static final String HELP = "help";
    private static final String SETTINGS = "settings";
    private static final String ABOUT = "about";
    private static final String XU = "xu";
    private static final String COMMAND_ROUNDED_CORNER = "/" + ROUNDED_CORNER;
    private static final String COMMAND_ROTATED = "/" + ROTATED;
    private static final String COMMAND_CIRCULAR = "/" + CIRCULAR;
    private static final String COMMAND_CROP = "/" + CROP;
    private static final String COMMAND_TRIM = "/" + TRIM;
    private static final String COMMAND_SHADOW = "/" + SHADOW;
    private static final String COMMAND_HIGHER = "/" + HIGHER;
    private static final String COMMAND_KEEP_AREA = "/" + KEEP_AREA;
    private static final String COMMAND_RESET = "/" + RESET;
    private static final String COMMAND_HELP = "/" + HELP;
    private static final String COMMAND_SETTINGS = "/" + SETTINGS;
    private static final String COMMAND_ABOUT = "/" + ABOUT;
    private static final String COMMAND_XU = "/" + XU;

    // callback data
    private static final String commandsFilename = "commands.txt";
    private static final String ENABLE_ROUNDED_CORNER = "enableRoundedCorner";
    private static final String DISABLE_ROUNDED_CORNER = "disableRoundedCorner";
    private static final String CANCEL_ROUNDED_CORNER = "cancelRoundedCorner";
    private static final String ENABLE_ROTATED = "enableRotated";
    private static final String DISABLE_ROTATED = "disableRotated";
    private static final String CANCEL_ROTATED = "cancelRotated";
    private static final String ENABLE_CIRCULAR = "enableCircular";
    private static final String DISABLE_CIRCULAR = "disableCircular";
    private static final String CANCEL_CIRCULAR = "cancelCircular";
    private static final String ENABLE_CROP = "enableCrop";
    private static final String DISABLE_CROP = "disableCrop";
    private static final String CANCEL_CROP = "cancelCrop";
    private static final String ENABLE_TRIM = "enableTrim";
    private static final String DISABLE_TRIM = "disableTrim";
    private static final String CANCEL_TRIM = "cancelTrim";
    private static final String ENABLE_SHADOW = "enableShadow";
    private static final String DISABLE_SHADOW = "disableShadow";
    private static final String CANCEL_SHADOW = "cancelShadow";
    private static final String ENABLE_HIGHER = "enableHigher";
    private static final String DISABLE_HIGHER = "disableHigher";
    private static final String CANCEL_HIGHER = "cancelHigher";
    private static final String KEEP_TOP_LEFT = "keepTopLeft";
    private static final String KEEP_TOP_CENTER = "keepTopCenter";
    private static final String KEEP_TOP_RIGHT = "keepTopRight";
    private static final String KEEP_CENTER_LEFT = "keepCenterLeft";
    private static final String KEEP_CENTER = "keepCenter";
    private static final String KEEP_CENTER_RIGHT = "keepCenterRight";
    private static final String KEEP_BOTTOM_LEFT = "keepBottomLeft";
    private static final String KEEP_BOTTOM_CENTER = "keepBottomCenter";
    private static final String KEEP_BOTTOM_RIGHT = "keepBottomRight";
    private static final String CANCEL_KEEP_AREA = "cancelKeepArea";
    private static final String MAKE_RESET = "makeReset";
    private static final String CANCEL_RESET = "cancelReset";

    private static int xu = 0;
    private static ByteBuffer bb = ByteBuffer.allocateDirect(1048576);

    public LiftAvatarBot() {
        try (BufferedReader bis = new BufferedReader(new FileReader("token"))) {
            token = bis.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageProc = new ImageProc();

        updateHelpText(commandsFilename);
    }

    private static void updateHelpText(String commandsFilename) {
        try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File(commandsFilename)))) {
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null && line.length() > 0) {
                sb.append('/');
                sb.append(line);
                sb.append('\n');
            }

            helpText = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        class TempClass implements Runnable {
            Update update;

            @Override
            public void run() {
                if (update.getMessage() != null) {
                    long chatId = update.getMessage().getChatId();
                    Settings settings = GetSettings.getSettings(chatId);

                    if (update.getMessage().isCommand()) {
                        String text = "Your current setting is ";
                        String[] commands = update.getMessage().getText().split(" ");
                        String command = commands[0];
                        InlineKeyboardMarkup markup = null;
                        switch (command) {
                            case COMMAND_ROUNDED_CORNER: {
                                text += settings.isRoundedCorner() + "\n" +
                                        "Press a bottom to set the value.";
                                markup = commandRoundedCorner();
                            }
                            break;
                            case COMMAND_ROTATED: {
                                text += settings.isRotated() + "\n" +
                                        "Press a bottom to set the value.";
                                markup = commandRotated();
                            }
                            break;
                            case COMMAND_CIRCULAR: {
                                text += settings.isCircular() + "\n" +
                                        "Press a bottom to set the value.";
                                markup = commandCircular();
                            }
                            break;
                            case COMMAND_CROP: {
                                text += settings.isCrop();
                                markup = commandCorp();
                            }
                            break;
                            case COMMAND_TRIM: {
                                text += settings.isTrim();
                                markup = commandTrim();
                            }
                            break;
                            case COMMAND_SHADOW: {
                                text += settings.isShadow();
                                markup = commandShadow();
                            }
                            break;
                            case COMMAND_HIGHER: {
                                text += settings.isHigher();
                                markup = commandHigher();
                            }
                            break;
                            case COMMAND_KEEP_AREA: {
                                text += getKeepArea(settings.getKeepArea()) + "\n" +
                                        "Press a bottom to set the value.";
                                markup = commandKeepArea();
                            }
                            break;
                            case COMMAND_RESET: {
                                text = "All your files will be deleted. Continue?\n" +
                                        "Press the bottom to confirm.";
                                markup = commandReset();
                            }
                            break;
                            case COMMAND_HELP: {
                                text = commandHelp(chatId);
                            }
                            break;
                            case COMMAND_SETTINGS: {
                                text = commandSettings(settings);
                            }
                            break;
                            case COMMAND_ABOUT: {
                                text = "You can Send me images right away! \nCreated by @a1prc ";
                            }
                            break;
                            case COMMAND_XU: {
                                try (BufferedReader br = new BufferedReader(new FileReader(XU))) {
                                    String line = br.readLine();
                                    xu = Integer.parseInt(line);
                                } catch (IOException e) {
                                    // no one +1s;
                                }
                                ++xu;
                                try (BufferedWriter bw = new BufferedWriter(new FileWriter(XU))) {
                                    bw.write(xu);
                                } catch (IOException e) {
                                    //
                                }
                                text = "+1s. " + xu + "s in total.";
                            }
                            break;
                            default:
                                text = "Say what?";
                        }

                        SendMessage message;
                        if (markup != null) {
                            message = new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText(text)
                                    .setReplyMarkup(markup);
                        } else {
                            message = new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText(text);
                        }

                        try {
                            sendMessage(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    // is message
                    if (update.hasMessage() &&
                            // sticker or photo or photo as file
                            (update.getMessage().getSticker() != null ||
                                    update.getMessage().hasPhoto() ||
                                    update.getMessage().hasDocument())) {
                        String FileId;
                        if (update.getMessage().hasPhoto()) { // receive photo
                            List<PhotoSize> li = update.getMessage().getPhoto();
                            int size = li.size();
                            FileId = li.get(size - 1).getFileId();
                        } else if (update.getMessage().getSticker() != null) { // receive sticker
                            FileId = update.getMessage().getSticker().getFileId();
                        } else { // as file.
                            FileId = update.getMessage().getDocument().getFileId();
                        }
                        byte[] imageInByteArray = downloadImages(FileId);

                        imageInByteArray = imageProc.hub(imageInByteArray, settings);

                        SendDocument document = new SendDocument()
                                .setChatId(update.getMessage().getChatId())
                                .setNewDocument(FileId + ".webp",
                                        new ByteArrayInputStream(imageInByteArray));

                        try {
                            sendDocument(document);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    return;
                }

                // is call back
                CallbackQuery cq;
                if ((cq = update.getCallbackQuery()) != null) {
                    String data = cq.getData();
                    long chatId = cq.getMessage().getChatId();
                    String confirmText;
                    switch (data) {
                        case ENABLE_ROUNDED_CORNER: {
                            confirmText = setRoundedCorner(chatId, true);
                        }
                        break;
                        case DISABLE_ROUNDED_CORNER: {
                            confirmText = setRoundedCorner(chatId, false);
                        }
                        break;
                        case ENABLE_ROTATED: {
                            confirmText = setRotated(chatId, true);
                        }
                        break;
                        case DISABLE_ROTATED: {
                            confirmText = setRotated(chatId, false);
                        }
                        break;
                        case ENABLE_CIRCULAR: {
                            confirmText = setCircular(chatId, true);
                        }
                        break;
                        case DISABLE_CIRCULAR: {
                            confirmText = setCircular(chatId, false);
                        }
                        break;
                        case ENABLE_CROP: {
                            confirmText = setCrop(chatId, true);
                        }
                        break;
                        case DISABLE_CROP: {
                            confirmText = setCrop(chatId, false);
                        }
                        break;
                        case ENABLE_TRIM: {
                            confirmText = setTrim(chatId, true);
                        }
                        break;
                        case DISABLE_TRIM: {
                            confirmText = setTrim(chatId, false);
                        }
                        break;
                        case ENABLE_SHADOW: {
                            confirmText = setShadow(chatId, true);
                        }
                        break;
                        case DISABLE_SHADOW: {
                            confirmText = setShadow(chatId, false);
                        }
                        break;
                        case ENABLE_HIGHER: {
                            confirmText = setHigher(chatId, true);
                        }
                        break;
                        case DISABLE_HIGHER: {
                            confirmText = setHigher(chatId, false);
                        }
                        break;
                        case KEEP_TOP_LEFT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_TOP_LEFT);
                        }
                        break;
                        case KEEP_TOP_CENTER: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_TOP_CENTER);
                        }
                        break;
                        case KEEP_TOP_RIGHT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_TOP_RIGHT);
                        }
                        break;
                        case KEEP_CENTER_LEFT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_CENTER_LEFT);
                        }
                        break;
                        case KEEP_CENTER: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_CENTER);
                        }
                        break;
                        case KEEP_CENTER_RIGHT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_CENTER_RIGHT);
                        }
                        break;
                        case KEEP_BOTTOM_LEFT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_BOTTOM_LEFT);
                        }
                        break;
                        case KEEP_BOTTOM_CENTER: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_BOTTOM_CENTER);
                        }
                        break;
                        case KEEP_BOTTOM_RIGHT: {
                            confirmText = setKeepArea(chatId, SquarifyImage.KEEP_CENTER_RIGHT);
                        }
                        break;
                        case MAKE_RESET: {
                            confirmText = makeReset(chatId);
                        }
                        break;
                        case CANCEL_ROUNDED_CORNER:
                        case CANCEL_ROTATED:
                        case CANCEL_CIRCULAR:
                        case CANCEL_CROP:
                        case CANCEL_TRIM:
                        case CANCEL_SHADOW:
                        case CANCEL_HIGHER:
                        case CANCEL_KEEP_AREA:
                        case CANCEL_RESET:{
                            confirmText = "Request has been canceled.";
                        }
                        break;
                        default:
                            confirmText = "Well of course I receive your... \nOh hey a bug.";
                    }

                    EditMessageText edit = new EditMessageText()
                            .setChatId(chatId)
                            .setText(confirmText)
                            .setMessageId(cq.getMessage().getMessageId());

                    try {
                        sendApiMethod(edit);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    return;
                }
            }

            TempClass(Update update) {
                this.update = update;
            }
        }

        Thread thread = new Thread(new TempClass(update));
        thread.start();
    }

    private byte[] downloadImages(String FileId) {
        GetFile gFile = new GetFile().setFileId(FileId);
        File file = null;
        try {
            file = getFile(gFile);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        if (file != null) {
            URL webUrl = null;
            try {
                webUrl = new URL("https://api.telegram.org/file/bot" + this.getBotToken() + "/" + file.getFilePath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (webUrl != null) {
                ReadableByteChannel rbc = null;
                try {
                    rbc = Channels.newChannel(webUrl.openStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (rbc != null) {
                    WritableByteChannel wbc = Channels.newChannel(output);
                    try {
                        bb.clear();
                        while (rbc.read(bb) > 0) {
                            bb.flip();
                            while (bb.hasRemaining()) {
                                wbc.write(bb);
                            }
                            bb.clear();
                        }
                        wbc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return output.toByteArray();
    }

    @Override
    public String getBotUsername() {
        return "LiftAvatarBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private static InlineKeyboardMarkup commandRoundedCorner() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());

        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_ROUNDED_CORNER));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_ROUNDED_CORNER));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setRoundedCorner(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_ROUNDED_CORNER, enable);
        return settingChangedMessage("rounded corner", enable);
    }

    private static InlineKeyboardMarkup commandRotated() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());

        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_ROTATED));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_ROTATED));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setRotated(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_ROTATED, enable);
        return settingChangedMessage("lift rotated images", enable);
    }

    private static InlineKeyboardMarkup commandCircular() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_CIRCULAR));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_CIRCULAR));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setCircular(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_CIRCULAR, enable);
        return settingChangedMessage("Crop images in circle shape", enable);
    }

    private static InlineKeyboardMarkup commandCorp() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_CROP));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_CROP));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setCrop(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_CROP, enable);
        return settingChangedMessage("crop image", enable);
    }

    private static InlineKeyboardMarkup commandTrim() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_TRIM));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_TRIM));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setTrim(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_TRIM, enable);
        return settingChangedMessage("trim", enable);
    }

    private static InlineKeyboardMarkup commandShadow() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_SHADOW));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_SHADOW));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setShadow(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_SHADOW, enable);
        return settingChangedMessage("shadow", enable);
    }

    private static InlineKeyboardMarkup commandHigher() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_HIGHER));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_HIGHER));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setHigher(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_HIGHER, enable);
        return settingChangedMessage("lift higher", enable);
    }

    private static InlineKeyboardMarkup commandKeepArea() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_TOP_LEFT));
        buttons.get(0).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_TOP_CENTER));
        buttons.get(0).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_TOP_RIGHT));
        buttons.add(new ArrayList<>());
        buttons.get(1).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_CENTER_LEFT));
        buttons.get(1).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_CENTER));
        buttons.get(1).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_CENTER_RIGHT));
        buttons.add(new ArrayList<>());
        buttons.get(2).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_BOTTOM_LEFT));
        buttons.get(2).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_BOTTOM_CENTER));
        buttons.get(2).add(new InlineKeyboardButton().setText("    ").setCallbackData(KEEP_BOTTOM_RIGHT));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setKeepArea(long chatId, int cropMethod) {
        editSettings(chatId, cropMethod);
        return settingChangedMessage("Crop area", getKeepArea(cropMethod));
    }

    private static InlineKeyboardMarkup commandReset() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Reset").setCallbackData(MAKE_RESET));

        return new InlineKeyboardMarkup()
                .setKeyboard(buttons);
    }

    private static String makeReset(long chatId) {
        deleteSettings(chatId);
        return "Okay, all your files have been deleted.";
    }

    private static String settingChangedMessage(String name, boolean status) {
        return "Your setting of " + name + " has been changed to " + status;
    }

    private static String settingChangedMessage(String name, String status) {
        return "Your setting of " + name + " has been changed to " + status;
    }

    private static String getKeepArea(int keepArea) {
        String area;
        switch (keepArea) {
            case SquarifyImage.KEEP_TOP_LEFT:
                area = "top left";
                break;
            case SquarifyImage.KEEP_TOP_CENTER:
                area = "top center";
                break;
            case SquarifyImage.KEEP_TOP_RIGHT:
                area = "top right";
                break;

            case SquarifyImage.KEEP_CENTER_LEFT:
                area = "center left";
                break;
            case SquarifyImage.KEEP_CENTER:
                area = "center";
                break;
            case SquarifyImage.KEEP_CENTER_RIGHT:
                area = "center right";
                break;

            case SquarifyImage.KEEP_BOTTOM_LEFT:
                area = "bottom left";
                break;
            case SquarifyImage.KEEP_BOTTOM_CENTER:
                area = "bottom center";
                break;
            case SquarifyImage.KEEP_BOTTOM_RIGHT:
                area = "bottom right";
                break;

            default:
                area = "undef";
        }

        return area;
    }

    private static String commandSettings(Settings settings) {
        String endl = "\n";

        String keepArea = getKeepArea(settings.getKeepArea());

        StringBuilder builder = new StringBuilder();
        builder.append("The bot's settings for you: ").append(endl)
                .append(endl);
        if (settings.isCircular()) builder.append("(Ignore) ");
        builder.append("Rounded Corner: ").append(settings.isRoundedCorner()).append(endl);
        builder.append("Rotated: ").append(settings.isRotated()).append(endl);
        builder.append("Circular: ").append(settings.isCircular()).append(endl);
        builder.append("Trim: ").append(settings.isTrim()).append(endl);
        builder.append("Shadow: ").append(settings.isTrim()).append(endl);
        if (settings.isRotated()) builder.append("(Ignored) ");
        builder.append("Higher: ").append(settings.isHigher()).append(endl);
        builder.append("Keep Area: ").append(keepArea).append(endl)
                .append(endl);
        builder.append("Send /help for more information.");

        return builder.toString();
    }

    private static String commandHelp(long chatId) {
        if (helpText == null) {
            updateHelpText(commandsFilename);
        }

        return helpText;
    }
}
