package ru.relex.SpringBabPog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired

    private static final HashMap<ChatId, Chat> chats = new HashMap<>(); //это хэш

    //public String PATH_TO_FILE = "C:/Users/endur/Documents/FilesFromTg/";
    public static final String HELP_TEXT =
            "Это бот, сохраняющий ваши файлы на компьютере\n\n" +
                    "Введите /start, чтобы запустить приветственное сообщение\n\n" +
                    "Введите /savedocument для сохранения документа\n\n" +
                    "Введите /getdocument для получения документа\n\n" +
                    "Введите /renamedocument для переименования документа\n\n" +
                    "Введите /deletedocument для удаления своих документов\n\n" +
                    "Введите /createnewpath для создания новой папки\n\n" +
                    "Введите /showdocuments, чтобы показать все сохраненные документы";

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
        ChatMessage ourChatMessage = ConvertToChatMessage(message);     //инициализируем наше сообщение, которое содержит текст или сообщение
        String textResponse = chat.MainAcceptMessage(ourChatMessage);      //это ответ, который тг бот отправляет в чат (посмотри реализацию acceptMessage)

        switch (textResponse) {
            case TextMessages.EXEC_SAVE -> SaveDocument(chat, ourChatMessage.getDocument());
            case TextMessages.EXEC_GET -> OutputDocument(chat, ourChatMessage.getText());
            case TextMessages.EXEC_PATH -> SetRepositoryPath(chat, ourChatMessage.getText());
            case TextMessages.EXEC_CREATE_PATH -> CreateFolder(chat, ourChatMessage.getText());
            case TextMessages.SHOW_MESSAGE -> ShowDocuments(chat);
            case TextMessages.EXEC_RENAME -> RenameDocument(chat, ourChatMessage.getText());
            case TextMessages.EXEC_DELETE -> DeleteDocument(chat, ourChatMessage.getText());
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

    private ChatMessage ConvertToChatMessage(Message telegramMessage) {
        return new ChatMessage(telegramMessage.getText(), GetChatDocument(telegramMessage));
    }


    private void CreateFolder(Chat chat, String repository) {
        String[] names = repository.split(";");
        String repositoryPath = names[0];
        String folderName = names[1];
        Path path = Paths.get(repositoryPath, folderName);
        ChatId chatId = chat.getChatId();

        if (Files.exists(path)) {
            sendMessage(chat.getChatId().getValue(), "Папка уже существует: " + path);
        } else {
            try {
                // Создаем директорию
                Files.createDirectories(path);
                sendMessage(chat.getChatId().getValue(), "Папка успешно создана: " + path);
            } catch (IOException e) {
                sendMessage(chat.getChatId().getValue(), "Ошибка при создании папки " + path);
                e.printStackTrace();
            }
        }
    }

    private ChatDocument GetChatDocument(Message telegramMessage) {
        Document telegramDocument = telegramMessage.getDocument();
        if (telegramDocument == null) {
            return null;
        }

        return new ChatDocument(telegramDocument.getFileName(), telegramDocument.getFileSize(), telegramDocument);
    }

    private void ShowDocuments(Chat chat) {
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File folder = new File(PATH_TO_FILE);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                StringBuilder sb = new StringBuilder();
                // Перебираем файлы и добавляем их имена в строку
                for (File file : files) {
                    sb.append(file.getName()).append("\n\n");
                }
                sendMessage(chat.getChatId().getValue(), TextMessages.SHOW_MESSAGE);
                sendMessage(chat.getChatId().getValue(), sb.toString());
                sendMessage(chat.getChatId().getValue(), "Введите /getdocument чтобы получить документ");
            } else {
                sendMessage(chat.getChatId().getValue(), "Папка пуста");
            }
        } else {
            sendMessage(chat.getChatId().getValue(), "Неверный указанный путь");
        }
    }

    private void OutputDocument(Chat chat, String documentName) {
        ChatId chatId = chat.getChatId();
        String PATH_TO_FILE = chat.getChatInfo().getPATH_TO_FILE();
        File file = new File(PATH_TO_FILE + documentName);
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
        }
    }

    private void RenameDocument(Chat chat, String documentName) {
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
        String typeoldName = GetFileType(oldName);
        String typenewName = GetFileType(newName);
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

    private String GetFileType(String filename) {
        int ind = filename.lastIndexOf('.');
        if (ind == -1) {
            return "";
        }
        else {
            return filename.substring(ind + 1);
        }
    }


    private void DeleteDocument(Chat chat, String documentName) {
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

        private void SetRepositoryPath(Chat chat, String path) {
        File folder = new File(path);
        if (folder.isDirectory()) {
            chat.getChatInfo().setPATH_TO_FILE(path);
            ChatId chatId = chat.getChatId();
            sendMessage(chatId.getValue(), "Путь изменен на " + path);
        }
        else {
            sendMessage(chat.getChatId().getValue(), "Неверный путь");
        }

    }

    private void SaveDocument(Chat chat, ChatDocument recievedDocument) {
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
        KeyboardSwitch(textToSend, message);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void KeyboardSwitch(String textToSend, SendMessage message) {
        switch (textToSend) {
            case TextMessages.SAVE_MESSAGE -> message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
            case TextMessages.PATH_MESSAGE -> message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
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
