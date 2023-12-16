package ru.relex.SpringBabPog.States;

import ru.relex.SpringBabPog.service.ChatMessage;
import ru.relex.SpringBabPog.service.ChatMessageHandlingResult;
import ru.relex.SpringBabPog.service.ChatState;
import ru.relex.SpringBabPog.service.TextMessages;

public final class CreateFolderState extends ChatState {

    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {
        return switch (message.getText()) {         //иначе вот
            case "/getnothing" -> new ChatMessageHandlingResult("Ничего не возвращено", new InitChatState());
            case "/back" -> new ChatMessageHandlingResult("Отмена ожидания файла", new InitChatState());
            default -> new ChatMessageHandlingResult(TextMessages.EXEC_CREATE_PATH, new InitChatState());
        };
    }
}
