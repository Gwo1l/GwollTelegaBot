package ru.relex.SpringBabPog.service;

import ru.relex.SpringBabPog.states.InitChatState;

public final class Chat
{
    private final ChatId id;


    private ChatInfo chatInfo;
    private ChatState currentState;     //текущее состояние (состояние - это состояние тг бота, когда он либо ничего не делает, либо ждет от тебя отправки, к примеру, документа)

    public Chat(ChatId id) {
        this.id = id;
        currentState = new InitChatState();     //начальное состояние (бот ничего не ожидает)
        chatInfo = new ChatInfo();      //что-то я не понял зачем нам тут создавать объект, если у него итак есть заполенные поля
    }
    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    public ChatId getChatId(){
        return id;
    }

    public String mainAcceptMessage(ChatMessage message){       //метод обрабатывающий сообщение
        ChatMessageHandlingResult result = currentState.AcceptMessage(message);
        ChatState nextState = result.getNextState();
        String textResponse = result.getTextResponse();
        currentState = nextState;
        return textResponse;
    }

}
