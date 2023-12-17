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


import java.nio.file.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired

    private static final HashMap<ChatId, Chat> chats = new HashMap<>(); //это хэш

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
    public void onUpdateReceived(Update update) {         //метод в котором происходит вся работа
        if (!update.hasMessage()) {
            return;
        }
        Message message = update.getMessage();      //извлекаем сообщение из update
        Chat chat = getOrCreateChat(message);    //инициализируем chat
        ChatMessage ourChatMessage = convertToChatMessage(message);     //инициализируем наше сообщение, которое содержит текст или сообщение
        String textResponse = chat.mainAcceptMessage(ourChatMessage);      //это ответ, который тг бот отправляет в чат (посмотри реализацию acceptMessage)

        switch (textResponse) {
            //case TextMessages.EXEC_SAVE -> saveDocument(chat, ourChatMessage.getDocument());
            case TextMessages.EXEC_TYPE -> executeType(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_CD -> executeCD(chat, ourChatMessage.getFileName());

            //case TextMessages.EXEC_PATH -> setRepositoryPath(chat, ourChatMessage.getText());
            case TextMessages.EXEC_MKDIR -> executeMKDIR(chat, ourChatMessage.getFileName());
            case TextMessages.EXEC_DIR -> executeDIR(chat);
            //case TextMessages.EXEC_RENAME -> renameDocument(chat, ourChatMessage.getText());
            //case TextMessages.EXEC_DELETE -> deleteDocument(chat, ourChatMessage.getText());
            default -> sendMessage(chat.getChatId().getValue(), textResponse);
        }

    }


    private Chat getOrCreateChat(Message message) {
        ChatId chatId = new ChatId(message.getChat().getId()); //получаем id чата
        if (chats.containsKey(chatId)) {      //проверяем был ли созднан у нас чат до этого (проверяем через хэшмап)
            return chats.get(chatId);       //и если был создан, то возвращаем этот чат
        }

        Chat chat = new Chat(chatId);    //а если чат до этого не создавался, то создаем чат
        chats.put(chatId, chat);
        return chat;
    }

    private ChatMessage convertToChatMessage(Message telegramMessage) {
        return new ChatMessage(telegramMessage.getText(), getChatDocument(telegramMessage));
    }


    private void executeMKDIR(Chat chat, String repository) {
        File newDirectory = new File(repository);
        try {
            if (!newDirectory.exists()) {
                boolean directoryCreated = newDirectory.mkdir();
                if (!directoryCreated) {
                    sendMessage(chat.getChatId().getValue(), "Не удалось создать папку");
                }
            } else {
                sendMessage(chat.getChatId().getValue(), "Папка " + repository + " уже существует");
            }
        } catch (SecurityException e) {
            sendMessage(chat.getChatId().getValue(),"Ошибка безопасности при попытке создать папку " + repository);
        }
        /*String[] names = repository.split(";");
        String repositoryPath = names[0];
        String folderName = names[1];
        Path path = Paths.get(repositoryPath, folderName);
        ChatId chatId = chat.getChatId();

        if (Files.exists(path)) {
            sendMessage(chat.getChatId().getValue(), "Папка уже существует: " + path);
        } else {
            try {

                Files.createDirectories(path);
                sendMessage(chat.getChatId().getValue(), "Папка успешно создана: " + path);
            } catch (IOException e) {
                sendMessage(chat.getChatId().getValue(), "Ошибка при создании папки " + path);
                e.printStackTrace();
            }
        }*/
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
                    }
                    else{
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
                sendMessage(chat.getChatId().getValue(), "Введите type или more чтобы получить документ");
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
        /*File file = new File(PATH_TO_FILE + documentName);
        if (file.exists()) {
            // Создание объекта InputFile из файла
            InputFile inputFile = new InputFile(file);

            // Создание объекта SendDocument
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.getValue()); // Установка ID чата для отправки документа
            sendDocument.setDocument(inputFile); // Установка документа для отправки

            try {
                // Отправка документа в телеграм боте
                execute(sendDocument);
                sendMessage(chatId.getValue(), "Файл выведен!");
                // Дополнительные действия после успешной отправки...
            } catch (TelegramApiException e) {
                e.printStackTrace();
                sendMessage(chatId.getValue(), "Ошибка вывода документа");
                // Действия в случае ошибки при отправке документа...
            }
        } else {
            sendMessage(chatId.getValue(), "Файл отсутствует или неправильное имя файла");
        }*/
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

    private void renameDocument(Chat chat, String documentName) {
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
        }
        else {
            return filename.substring(ind + 1);
        }
    }


    private void deleteDocument(Chat chat, String documentName) {
        ChatId chatId = chat.getChatId();
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File file = new File(PATH_TO_FILE + documentName);
        if (file.exists()) {
            boolean deleted = file.delete(); // Удаление файла
            if (deleted) {
                sendMessage(chatId.getValue(), "Файл удалён!");
                // Дополнительные действия после успешного удаления...
            } else {
                sendMessage(chatId.getValue(), "Ошибка удаления документа");
                // Действия в случае ошибки при удалении файла...
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
            }
            else {
                sendMessage(chatId.getValue(), PATH_TO_FILE);
            }
        }
        else {
            String pathRepository = PATH_TO_FILE + repository + "/";
            File folder = new File(pathRepository);
            if (folder.isDirectory()) {
                chat.getChatInfo().setPATH_TO_FILE(pathRepository);
                sendMessage(chatId.getValue(), pathRepository);
            }
            else {
                sendMessage(chat.getChatId().getValue(), "Не существует такого репозитория");
            }
        }

    }

    private void saveDocument(Chat chat, ChatDocument recievedDocument) {
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

    private void sendMessage(long chatId, String textToSend)  {
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
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void keyboardSwitch(String textToSend, SendMessage message) {
        switch (textToSend) {
            case TextMessages.SAVE_MESSAGE -> message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
            case TextMessages.EXEC_CD -> message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
        }
    }

    private ReplyKeyboardMarkup TelegramKeyboard(String firstCommand, String secondCommand) {
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

    //        if (update.hasMessage() && update.getMessage().hasText()) {     //проверяем что нам пришло сообщение и что в нём есть текст
//            String messageText = update.getMessage().getText();     //объект, который является самим сообщением
//            long chatId = update.getMessage().getChatId();      //айди, чтобы бот отправлял сообщение пользователю с этим айди
//            switch (messageText) {
//                case "/start":
//                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());       //метод который здоровается с пользователем по имени (ниже его реализация)
//                        if (update.getMessage().hasDocument()) {        //если прислали документ
//                            Message message = update.getMessage();
//                            Document document = message.getDocument();
//                            try {
//                                GetFile getFile = new GetFile();
//                                getFile.setFileId(document.getFileId());
//                                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
//
//                                String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
//
//                                URL url = new URL(fileUrl);
//                                InputStream in = url.openStream();
//                                Files.copy(in, Paths.get("files/" + document.getFileName()), StandardCopyOption.REPLACE_EXISTING);
//                                in.close();
//                            }
//                            catch (TelegramApiException | MalformedURLException e) {
//                                e.printStackTrace();
//                            }
//                            catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        break;
//                case "/help":
//                        sendMessage(chatId, HELP_TEXT);     //реализация ниже
//                        break;
//                default:
//
//                        sendMessage(chatId, "Я тебя не понял. Спроси ещё раз!");
//            }
//        }
}
