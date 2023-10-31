package ru.relex.SpringBabPog.service;

public final class NameOfDocumentState extends ChatState {

    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {
        return switch (message.getText()) {         //иначе вот
            case "/getnothing" -> new ChatMessageHandlingResult("Ничего не возвращено", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания файла", new InitChatState());
            default -> new ChatMessageHandlingResult("Введите имя файла", new InitChatState());
        };
    }
}
