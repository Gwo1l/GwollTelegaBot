package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.ChatMessage;
import ru.relex.SpringBabPog.service.ChatMessageHandlingResult;
import ru.relex.SpringBabPog.service.ChatState;
import ru.relex.SpringBabPog.service.TextMessages;

public class RepositoryPathState extends ChatState {
    @Override
    public ChatMessageHandlingResult acceptMessage(ChatMessage message) {
        return switch (message.getText()) {         //иначе вот
            case "/getnothing" -> new ChatMessageHandlingResult("Ничего не возвращено", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания ввода пути", new InitChatState());
            default -> new ChatMessageHandlingResult(TextMessages.EXEC_PATH, new InitChatState());
        };
    }
}
