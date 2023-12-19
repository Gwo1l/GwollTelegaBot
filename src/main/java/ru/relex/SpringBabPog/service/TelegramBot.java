package ru.relex.SpringBabPog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.relex.SpringBabPog.config.BotConfig;

import java.awt.Desktop;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;



@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired

    private static final HashMap<ChatId, Chat> chats = new HashMap<>();

    //public String PATH_TO_FILE = "C:/Users/endur/Documents/FilesFromTg/";
    public static final String HELP_TEXT =
            "Это бот, сохраняющий ваши файлы на компьютере\n\n" +
                    "Введите /start, чтобы запустить приветственное сообщение\n\n" +
                    "Введите save для сохранения документа\n\n" +
                    "Введите type или more для получения документа\n\n" +
                    "Введите ren или rename для переименования документа\n\n" +
                    "Введите del или erase для удаления своих документов\n\n" +
                    "Введите mkdir для создания новой папки\n\n" +
                    "Введите cd для изменения директории\n\n" +
                    "Введите dir, чтобы показать все сохраненные документы";

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        Message message = update.getMessage();
        Chat chat = getOrCreateChat(message);
        ChatMessage ourChatMessage = convertToChatMessage(message);
        String textResponse = chat.mainAcceptMessage(ourChatMessage);

        switch (textResponse) {
            case TextMessages.EXEC_TYPE -> executeType(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_CD -> executeCD(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_START -> executeStart(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_MKDIR -> executeMKDIR(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_DIR -> executeDIR(chat);
            case TextMessages.EXEC_REN -> executeREN(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_DEL -> executeDEL(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_SAVE -> executeSave(chat, ourChatMessage.getDocument());
            default -> sendMessage(chat.getChatId().getValue(), textResponse);
        }

    }

    private void executeStart(Chat chat, String fileName) {
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        try {
            File file = new File(PATH_TO_FILE + fileName);
            if (file.exists()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            } else {
                sendMessage(chat.getChatId().getValue(), "Файл " + fileName + " не существует");
            }
        } catch (IOException e) {
            sendMessage(chat.getChatId().getValue(), "Произошла ошибка при открытии файла: " + e.getMessage());
        }

    }


    private Chat getOrCreateChat(Message message) {
        ChatId chatId = new ChatId(message.getChat().getId());
        if (chats.containsKey(chatId)) {
            return chats.get(chatId);
        }

        Chat chat = new Chat(chatId);
        chats.put(chatId, chat);
        return chat;
    }

    private ChatMessage convertToChatMessage(Message telegramMessage) {
        return new ChatMessage(telegramMessage.getText(), getChatDocument(telegramMessage));
    }


    private void executeMKDIR(Chat chat, String repository) {
        File newDirectory = new File(chat.getChatInfo().getPATH_TO_FILE() + repository);
        try {
            if (!newDirectory.exists()) {
                boolean directoryCreated = newDirectory.mkdirs();
                if (!directoryCreated) {
                    sendMessage(chat.getChatId().getValue(), "Не удалось создать папку");
                } else {
                    sendMessage(chat.getChatId().getValue(), "Папка " + repository + " успешно создана");
                }
            } else {
                sendMessage(chat.getChatId().getValue(), "Папка " + repository + " уже существует");
            }
        } catch (SecurityException e) {
            sendMessage(chat.getChatId().getValue(),
                    "Ошибка безопасности при попытке создать папку " + repository);
        }
    }

    private void executeSave(Chat chat, ChatDocument recievedDocument) {
        Document document = recievedDocument.document();
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
            URL url = new URL(fileUrl);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get(chat.getChatInfo().getPATH_TO_FILE() + document.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            sendMessage(chat.getChatId().getValue(), "Документ сохранен!");
        } catch (TelegramApiException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ChatDocument getChatDocument(Message telegramMessage) {
        Document telegramDocument = telegramMessage.getDocument();
        if (telegramDocument == null) {
            return null;
        }

        return new ChatDocument(telegramDocument.getFileName(), telegramDocument.getFileSize(), telegramDocument);
    }

    private void executeDIR(Chat chat) {
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File folder = new File(PATH_TO_FILE);
        int countDirectory = 0;
        int countFile = 0;
        double summaryFileSize = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (File file : files) {
                    long timestamp = file.lastModified();
                    Date creationDate = new Date(timestamp);
                    if (file.isDirectory()) {
                        countDirectory += 1;
                        sb.append(creationDate).append("   <DIR>   ").append(file.getName()).append("\n\n");
                    } else {
                        long fileSize = file.length();
                        summaryFileSize += fileSize;
                        countFile += 1;
                        sb.append(creationDate).append("   ").append(fileSize).append("    ").append(file.getName()).append("\n\n");
                    }
                }
                String filesStatistics = countFile + " файлов  " + summaryFileSize + " байт";
                String repositoryStatistics = countDirectory + " папок";
                sendMessage(chat.getChatId().getValue(), TextMessages.EXEC_DIR);
                sendMessage(chat.getChatId().getValue(), sb.toString());
                sendMessage(chat.getChatId().getValue(), filesStatistics);
                sendMessage(chat.getChatId().getValue(), repositoryStatistics);
            } else {
                sendMessage(chat.getChatId().getValue(), "Папка пуста");
            }
        } else {
            sendMessage(chat.getChatId().getValue(), "Неверный указанный путь");
        }
    }

    private void executeType(Chat chat, String documentName) {
        ChatId chatId = chat.getChatId();
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        try {
            File file = new File(PATH_TO_FILE + documentName);
            if (!file.createNewFile()) {
                sendMessage(chatId.getValue(), "Файл уже создан");
            }
        } catch (IOException e) {
            sendMessage(chatId.getValue(), "Произошла ошибка при создании файла.");
            e.printStackTrace();
        }
    }

    private void executeREN(Chat chat, String documentName) {
        String[] names = documentName.split(":");
        String oldName = names[0];
        String newName = names[1];
        char lastSymb = oldName.charAt(oldName.length() - 1);
        char firstSymb = newName.charAt(0);
        if (lastSymb == ' ') {
            oldName = oldName.substring(0, oldName.length() - 1);
        }
        if (firstSymb == ' ') {
            newName = newName.substring(1);
        }
        ChatId chatId = chat.getChatId();
        if (oldName.equals(newName)) {
            sendMessage(chatId.getValue(), "Имена файлов совпадают");
            return;
        }
        String typeoldName = getFileType(oldName);
        String typenewName = getFileType(newName);
        if (!typenewName.equals(typeoldName)) {
            sendMessage(chatId.getValue(), "Нельзя переименовать файл, т.к типы не совпадают");
            return;
        }
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File oldFile = new File(PATH_TO_FILE + oldName);
        File newFile = new File(PATH_TO_FILE + newName);
        if (oldFile.exists()) {
            boolean isRenamed = oldFile.renameTo(newFile);
            if (isRenamed) {
                sendMessage(chatId.getValue(), "Файл успешно переименован!");
            } else {
                sendMessage(chatId.getValue(), "Не удалось переименовать файл");
            }
        } else {
            sendMessage(chatId.getValue(), ("Файл " + oldName + " не существует"));
        }
    }

    private String getFileType(String filename) {
        int ind = filename.lastIndexOf('.');
        if (ind == -1) {
            return "";
        } else {
            return filename.substring(ind + 1);
        }
    }


    private void executeDEL(Chat chat, String documentName) {
        ChatId chatId = chat.getChatId();
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File file = new File(PATH_TO_FILE + documentName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                sendMessage(chatId.getValue(), "Файл удалён!");
            } else {
                sendMessage(chatId.getValue(), "Ошибка удаления документа");
            }
        } else {
            sendMessage(chatId.getValue(), "Файл отсутствует или неправильное имя файла");
        }
    }

    private void executeCD(Chat chat, String repository) {
        ChatId chatId = chat.getChatId();
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        if (Objects.equals(repository, "..")) {
            int lastSeparatorIndex = PATH_TO_FILE.substring(0, PATH_TO_FILE.length() - 1).lastIndexOf('/');
            if (lastSeparatorIndex != -1) {
                chat.getChatInfo().setPATH_TO_FILE(PATH_TO_FILE.substring(0, lastSeparatorIndex + 1));
                sendMessage(chatId.getValue(), chat.getChatInfo().getPATH_TO_FILE());
            } else {
                sendMessage(chatId.getValue(), PATH_TO_FILE);
            }
        } else {
            String pathRepository = PATH_TO_FILE + repository + "/";
            File folder = new File(pathRepository);
            if (folder.isDirectory()) {
                chat.getChatInfo().setPATH_TO_FILE(pathRepository);
                sendMessage(chatId.getValue(), pathRepository);
            } else {
                sendMessage(chat.getChatId().getValue(), "Не существует такого репозитория");
            }
        }

    }



    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));      //задать айди сообщению, чтоб знал куда отправлять
        message.setText(textToSend);

        /*if (Objects.equals(textToSend, TextMessages.SAVE_MESSAGE)) {
            message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
        } else if (Objects.equals(textToSend, TextMessages.GET_MESSAGE) || Objects.equals(textToSend, TextMessages.DELETE_MESSAGE)) {
            message.setReplyMarkup(TelegramKeyboard("/back", "/getnothing"));
        }*/
        keyboardSwitch(textToSend, message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void keyboardSwitch(String textToSend, SendMessage message) {
        switch (textToSend) {
            case TextMessages.EXEC_SAVE -> message.setReplyMarkup(telegramKeyboard("/back", "/document"));
            case TextMessages.EXEC_CD -> message.setReplyMarkup(telegramKeyboard("/back", "/document"));
        }
    }

    private ReplyKeyboardMarkup telegramKeyboard(String firstCommand, String secondCommand) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(firstCommand);
        row.add(secondCommand);
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}


