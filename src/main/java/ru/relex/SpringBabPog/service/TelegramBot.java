package ru.relex.SpringBabPog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.relex.SpringBabPog.config.BotConfig;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

    static final String PATH_TO_FILE = "C:/Users/endur/Documents/FilesFromTg/";
    static final String HELP_TEXT =
            "It's the bot saving your files on Yandex Disk\n\n" +
            "Type /start to send your document\n\n" +
            "Type /mydocuments to see your documents\n\n" +
            "Type /deletedocuments to delete your documents";
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
            SaveDocument(ourChatMessage.getDocument());
        }
        if (textResponse == null) {
            OutputDocument(ourChatMessage.getText());
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

    private String OutputDocument(String myPath) {
        return "nothing";
    }

    private void SaveDocument(ChatDocument recievedDocument) {
        Document document = recievedDocument.document();
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(document.getFileId());
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
            URL url = new URL(fileUrl);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get(PATH_TO_FILE + document.getFileName()), StandardCopyOption.REPLACE_EXISTING);
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




        KeyboardOfDocumentState keyboardDoc = new KeyboardOfDocumentState();    //клавиатура (пока что статичная клава)
        keyboardDoc.KeyboardFunc();
        message.setReplyMarkup(keyboardDoc.getKeyboardMarkup());




        try {

            execute(message);
        }
        catch (TelegramApiException e) {
            //здесь должен быть логер
            e.printStackTrace();
        }
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
