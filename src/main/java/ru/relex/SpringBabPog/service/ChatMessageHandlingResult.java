package ru.relex.SpringBabPog.service;

public final class ChatMessageHandlingResult    //нужная хрень короче
{
    private final String text;
    private final ChatState nextState;

    public ChatMessageHandlingResult(String text, ChatState nextState) {
        this.text = text;
        this.nextState = nextState;
    }

    public String getTextResponse(){
        return text;
    }

    public ChatState getNextState(){
        return nextState;
    }
}
