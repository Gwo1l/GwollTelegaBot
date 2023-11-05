package ru.relex.SpringBabPog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.relex.SpringBabPog.config.BotConfig;


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

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired

    private static final HashMap<ChatId, Chat> chats = new HashMap<>(); //это хэш

    //public String PATH_TO_FILE = "C:/Users/endur/Documents/FilesFromTg/";
    public static final String HELP_TEXT =
            "It's the bot saving your files on your PC\n\n" +
            "Type /start to start a welcome message\n\n" +
            "Type /savedocument to save your document\n\n" +
            "Type /getdocument to get document\n\n" +
            "Type /deletedocument to delete your documents\n\n" +
            "Type /showdocuments to show all your saved documents";
    final BotConfig config;
    public TelegramBot(BotConfig config) {
        this.config = config;
    }
    @Override
    public void onUpdateReceived(Update update) {         //метод в котором происходит вся работа
        if (!update.hasMessage())
        {
            return;
        }
        Message message = update.getMessage();      //извлекаем сообщение из update
        Chat chat = getOrCreateChat(message);    //инициализируем chat
        ChatMessage ourChatMessage = ConvertToChatMessage(message);     //инициализируем наше сообщение, которое содержит текст или сообщение
        String textResponse = chat.MainAcceptMessage(ourChatMessage);      //это ответ, который тг бот отправляет в чат (посмотри реализацию acceptMessage)
        if (ourChatMessage.getDocument() != null) {
            SaveDocument(chat, ourChatMessage.getDocument());
        }
        if (Objects.equals(textResponse, "Введите имя файла")) {
            OutputDocument(chat, ourChatMessage.getText());
        }
        else if (Objects.equals(textResponse, "Введите путь")) {
            SetRepositoryPath(chat, ourChatMessage.getText());
        }
        else {
            sendMessage(chat.getChatId().getValue(), textResponse);     //сам метод отправки сообщения
        }

    }

    private Chat getOrCreateChat(Message message){
        ChatId chatId = new ChatId(message.getChat().getId()); //получаем id чата
        if(chats.containsKey(chatId)){      //проверяем был ли созднан у нас чат до этого (проверяем через хэшмап)
            return chats.get(chatId);       //и если был создан, то возвращаем этот чат
        }

        Chat chat = new Chat(chatId);    //а если чат до этого не создавался, то создаем чат
        chats.put(chatId, chat);
        return chat;
    }

    private ChatMessage ConvertToChatMessage(Message telegramMessage){
        return new ChatMessage(telegramMessage.getText(), GetChatDocument(telegramMessage));
    }

    private ChatDocument GetChatDocument(Message telegramMessage){
        Document telegramDocument = telegramMessage.getDocument();
        if(telegramDocument == null){
            return null;
        }

        return new ChatDocument(telegramDocument.getFileName(), telegramDocument.getFileSize(), telegramDocument);
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
        }
        else {
            sendMessage(chatId.getValue(), "Файл отсутствует или неправильное имя файла");
        }
    }

    private void SetRepositoryPath(Chat chat, String path) {
        chat.getChatInfo().setPATH_TO_FILE(path);
        ChatId chatId = chat.getChatId();
        sendMessage(chatId.getValue(), "Путь изменен на " + path);
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

        if (Objects.equals(textToSend, "Отправь свой документ")) {
            message.setReplyMarkup(TelegramKeyboard("/back", "/document"));
        } else if (Objects.equals(textToSend, "Введи имя файла (вместе с расширением)")) {
            message.setReplyMarkup(TelegramKeyboard("/back", "/getnothing"));
        }

        try {

            execute(message);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
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
