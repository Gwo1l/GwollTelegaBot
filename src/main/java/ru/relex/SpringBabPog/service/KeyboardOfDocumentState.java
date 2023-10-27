package ru.relex.SpringBabPog.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardOfDocumentState {
    private ReplyKeyboardMarkup keyboardMarkup;

    /*public KeyboardOfDocumentState() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    }*/


    public void KeyboardFunc() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/back");
        row.add("/document");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
    }
    public ReplyKeyboardMarkup getKeyboardMarkup() {
        return keyboardMarkup;
    }

    public void setKeyboardMarkup(ReplyKeyboardMarkup keyboardMarkup) {
        this.keyboardMarkup = keyboardMarkup;
    }

}
