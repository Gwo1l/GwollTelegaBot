package ru.relex.SpringBabPog.service;

public class RepositoryPathState extends ChatState{
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {
        return switch (message.getText()) {         //иначе вот
            case "/getnothing" -> new ChatMessageHandlingResult("Ничего не возвращено", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания ввода пути", new InitChatState());
            default -> new ChatMessageHandlingResult("Введите путь", new InitChatState());
        };
    }
}
