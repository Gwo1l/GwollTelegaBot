package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.ChatMessage;
import ru.relex.SpringBabPog.service.ChatMessageHandlingResult;
import ru.relex.SpringBabPog.service.ChatState;
import ru.relex.SpringBabPog.service.TextMessages;

public class RenameChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {
        return switch (message.getText()) {
            case "/getnothing" -> new ChatMessageHandlingResult("Ничего не возвращено", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания имени файла", new InitChatState());
            default -> new ChatMessageHandlingResult(TextMessages.EXEC_RENAME, new InitChatState());
        };
    }
}