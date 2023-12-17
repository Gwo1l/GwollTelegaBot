package ru.relex.SpringBabPog.service;

import org.telegram.telegrambots.meta.api.objects.Document;

public final class ChatMessage
{
    private final String text;


    private final String fileName;
    private final ChatDocument document;

    public ChatMessage(String text, ChatDocument document) {
        if (text.contains(" ")) {
            String[] spl = text.split(" ");
            this.text = spl[0];
            this.fileName = spl[1];
        }
        else {
            this.text = text;
            this.fileName = null;
        }
        this.document = document;
    }

    public String getText(){
        return text;
    }

    public String getFileName() {
        return fileName;
    }


    public ChatDocument getDocument() {
        return document;
    }
}
