package ru.relex.SpringBabPog.service;

import org.telegram.telegrambots.meta.api.objects.Document;

public final class ChatMessage
{
    private final String text;
    private final ChatDocument document;

    public ChatMessage(String text, ChatDocument document) {
        this.text = text;
        this.document = document;
    }

    public String getText(){
        return text;
    }

    public ChatDocument getDocument() {
        return document;
    }
}
