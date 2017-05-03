package moe.alprc.liftAvatarBot;

import moe.alprc.liftAvatarBot.image.ImageProc;
import moe.alprc.liftAvatarBot.image.SquarifyImage;
import moe.alprc.liftAvatarBot.settings.GetSettings;
import moe.alprc.liftAvatarBot.settings.Settings;
import org.telegram.telegrambots.TelegramBotsApi;
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
import java.util.Random;

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
    private static final String ENABLE_ROUNDED_CORNER = "0";
    private static final String DISABLE_ROUNDED_CORNER = "1";
    private static final String CANCEL_ROUNDED_CORNER = "2";
    private static final String ENABLE_ROTATED = "3";
    private static final String DISABLE_ROTATED = "4";
    private static final String CANCEL_ROTATED = "5";
    private static final String ENABLE_CIRCULAR = "6";
    private static final String DISABLE_CIRCULAR = "7";
    private static final String CANCEL_CIRCULAR = "8";
    private static final String ENABLE_CROP = "9";
    private static final String DISABLE_CROP = "A";
    private static final String CANCEL_CROP = "B";
    private static final String ENABLE_TRIM = "C";
    private static final String DISABLE_TRIM = "D";
    private static final String CANCEL_TRIM = "E";
    private static final String ENABLE_SHADOW = "F";
    private static final String DISABLE_SHADOW = "G";
    private static final String CANCEL_SHADOW = "H";
    private static final String ENABLE_HIGHER = "I";
    private static final String DISABLE_HIGHER = "J";
    private static final String CANCEL_HIGHER = "K";
    private static final String KEEP_TOP_LEFT = "L";
    private static final String KEEP_TOP_CENTER = "M";
    private static final String KEEP_TOP_RIGHT = "N";
    private static final String KEEP_CENTER_LEFT = "O";
    private static final String KEEP_CENTER = "P";
    private static final String KEEP_CENTER_RIGHT = "Q";
    private static final String KEEP_BOTTOM_LEFT = "R";
    private static final String KEEP_BOTTOM_CENTER = "S";
    private static final String KEEP_BOTTOM_RIGHT = "T";
    private static final String CANCEL_KEEP_AREA = "U";
    private static final String MAKE_RESET = "V";
    private static final String CANCEL_RESET = "W";

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
            private Update update;

            @Override
            public void run() {
                // is message.
                if (update.hasMessage()) {
                    long chatId = update.getMessage().getChatId();
                    Settings settings = GetSettings.getSettings(chatId);
                    String username = update.getMessage().getFrom().getUserName();

                    if (update.getMessage().isCommand()) {
                        String text = "Your current setting is ";
                        String[] commands = update.getMessage().getText().split(" ");
                        String command = commands[0];

                        System.out.println(username + "[" + chatId + "] " + command);

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
                                    bw.write("" + xu);
                                } catch (IOException e) {
                                    //
                                }
                                text = "+1s. (" + xu + "s).";
                            }
                            break;
                            default:
                                text = "Unrecognized command. Say what?";
                        }

                        SendMessage message;
                        if (markup != null) {
                            message = new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText(text)
                                    .setReplyMarkup(markup)
                                    .setReplyToMessageId(update.getMessage().getMessageId());
                        } else {
                            message = new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText(text)
                                    .setReplyToMessageId(update.getMessage().getMessageId());
                        }

                        try {
                            sendMessage(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    // sticker or photo or photo as file
                    if ((update.getMessage().getSticker() != null ||
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

                        System.out.println(username + "[" + chatId + "] " + FileId);

                        imageInByteArray = imageProc.hub(imageInByteArray, settings);

                        SendDocument document = new SendDocument()
                                .setChatId(update.getMessage().getChatId())
                                .setNewDocument(FileId + ".webp",
                                        new ByteArrayInputStream(imageInByteArray))
                                .setReplyToMessageId(update.getMessage().getMessageId());

                        try {
                            sendDocument(document);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    // is message
                    if (update.getMessage().hasText()) {
                        System.out.println(username + "[" + chatId + "] " + update.getMessage().getText());

                        class GeneText {
                            private final String[] rep = {
                                    "Obviously",
                                    "Obviously not",
                                    "That doesn't sound very tasty at all",
                                    "What's wrong?",
                                    "Do you want to talk about it?",
                                    "Get out",
                                    "Not a question",
                                    "That's a stupid question",
                                    "Not really.",
                                    "If it's from you... I don't mind.",
                                    "NO.",
                                    "YES.",
                                    "I'm sure you understand. And if you don't maybe you should.",
                            };

                            private String randomText() {
                                Random random = new Random(System.currentTimeMillis());
                                return rep[Math.abs(random.nextInt()) % rep.length];
                            }
                        }

                        SendMessage message = new SendMessage()
                                .setChatId(update.getMessage().getChatId())
                                .setReplyToMessageId(update.getMessage().getMessageId())
                                .setText(new GeneText().randomText());

                        try {
                            sendMessage(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    return;
                }

                // is call back
                CallbackQuery cq;
                if ((cq = update.getCallbackQuery()) != null) {
                    String data = cq.getData();
                    long chatId = cq.getMessage().getChatId();
                    String username = cq.getMessage().getFrom().getUserName();

                    System.out.println(username + "[" + chatId + "] " + data);


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
                            confirmText = "Operation has been canceled.";
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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_ROUNDED_CORNER));

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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_ROTATED));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setRotated(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_ROTATED, enable);
        return settingChangedMessage("rotate the images", enable);
    }

    private static InlineKeyboardMarkup commandCircular() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Enable").setCallbackData(ENABLE_CIRCULAR));
        buttons.get(0).add(new InlineKeyboardButton().setText("Disable").setCallbackData(DISABLE_CIRCULAR));
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_CIRCULAR));

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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_CROP));

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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_TRIM));

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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_SHADOW));

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
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_HIGHER));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setHigher(long chatId, boolean enable) {
        editSettings(chatId, GetSettings.EDIT_HIGHER, enable);
        return settingChangedMessage("lift higher", enable);
    }

    private static InlineKeyboardMarkup commandKeepArea() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_TOP_LEFT));
        buttons.get(0).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_TOP_CENTER));
        buttons.get(0).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_TOP_RIGHT));
        buttons.add(new ArrayList<>());
        buttons.get(1).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_CENTER_LEFT));
        buttons.get(1).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_CENTER));
        buttons.get(1).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_CENTER_RIGHT));
        buttons.add(new ArrayList<>());
        buttons.get(2).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_BOTTOM_LEFT));
        buttons.get(2).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_BOTTOM_CENTER));
        buttons.get(2).add(new InlineKeyboardButton().setText(" ").setCallbackData(KEEP_BOTTOM_RIGHT));
        buttons.add(new ArrayList<>());
        buttons.get(3).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_KEEP_AREA));

        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }

    private static String setKeepArea(long chatId, int cropMethod) {
        editSettings(chatId, cropMethod);
        return settingChangedMessage("Keep area", getKeepArea(cropMethod));
    }

    private static InlineKeyboardMarkup commandReset() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(new ArrayList<>());
        buttons.get(0).add(new InlineKeyboardButton().setText("Reset").setCallbackData(MAKE_RESET));
        buttons.get(0).add(new InlineKeyboardButton().setText("Cancel").setCallbackData(CANCEL_RESET));

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
        builder.append("Your current settings are: ").append(endl)
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
