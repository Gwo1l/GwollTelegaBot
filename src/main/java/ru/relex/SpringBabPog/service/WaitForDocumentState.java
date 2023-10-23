package ru.relex.SpringBabPog.service;

public class WaitForDocumentState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {       //реализация acceptMessage в контесте документа
        if (message.getDocument() != null) {    //проверяем пришел ли нам документ
            return new ChatMessageHandlingResult("Документ принят", new InitChatState());   //отправляем сообщение документ принят и возвращаемся в начальное состояние
        }
        return switch (message.getText()){         //иначе вот
            case "/document" -> new ChatMessageHandlingResult("Документ принят", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена, назад", new InitChatState());
            default -> new ChatMessageHandlingResult("Я тебя не понял, отправь документ или введи /back", this);
        };
    }
}
