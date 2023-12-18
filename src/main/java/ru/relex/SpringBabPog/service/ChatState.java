package ru.relex.SpringBabPog.service;

public abstract class ChatState
{
    public abstract ChatMessageHandlingResult acceptMessage(ChatMessage message);

}
