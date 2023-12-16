package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.ChatMessage;
import ru.relex.SpringBabPog.service.ChatMessageHandlingResult;
import ru.relex.SpringBabPog.service.ChatState;
import ru.relex.SpringBabPog.service.TextMessages;

//import static jdk.javadoc.internal.tool.Main.execute;

public class WaitForDocumentState extends ChatState {



    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {       //реализация acceptMessage в контесте документа
        if (message.getDocument() != null) {    //проверяем пришел ли нам документ

            //Document document = message.getDocument();
            //GetFile getFile = new GetFile();
            //getFile.setFileId(document.getFileId());
            //org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            return new ChatMessageHandlingResult(TextMessages.EXEC_SAVE, new InitChatState());   //отправляем сообщение документ принят и возвращаемся в начальное состояние
        }

        return switch (message.getText()){         //иначе вот
            case "/document" -> new ChatMessageHandlingResult("Документ принят", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания файла", new InitChatState());
            default -> new ChatMessageHandlingResult("Я тебя не понял, отправь документ или введи /back", this);
        };
    }
}
