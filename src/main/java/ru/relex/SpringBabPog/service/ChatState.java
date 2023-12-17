package ru.relex.SpringBabPog.service;

public abstract class ChatState     //абстрактный класс (какое-то состояние) которого потом реализуют классы InitChatState и
{
    public abstract ChatMessageHandlingResult acceptMessage(ChatMessage message);

}
