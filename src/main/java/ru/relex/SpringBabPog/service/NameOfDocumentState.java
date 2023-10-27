package ru.relex.SpringBabPog.service;

public final class NameOfDocumentState extends ChatState {

    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message) {
        return new ChatMessageHandlingResult(null, new InitChatState());
    }
}
