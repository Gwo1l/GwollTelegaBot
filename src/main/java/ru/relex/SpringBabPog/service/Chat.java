package ru.relex.SpringBabPog.service;

public final class Chat
{
    private final ChatId id;
    private ChatState currentState;     //текущее состояние (состояние - это состояние тг бота, когда он либо ничего не делает, либо ждет от тебя отправки, к примеру, документа)

    public Chat(ChatId id) {
        this.id = id;
        currentState = new InitChatState();     //начальное состояние (бот ничего не ожидает)
    }

    public ChatId getChatId(){
        return id;
    }

    public String MainAcceptMessage(ChatMessage message){       //метод обрабатывающий сообщение
        ChatMessageHandlingResult result = currentState.AcceptMessage(message);
        ChatState nextState = result.getNextState();
        String textResponse = result.getTextResponse();
        currentState = nextState;
        return textResponse;
    }

}
