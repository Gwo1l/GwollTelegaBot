package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.*;



public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult acceptMessage(ChatMessage message)     //реализация acceptMessage (начальное состояние)
    {
        return switch (message.getText()) {
            case "save", "SAVE" -> new ChatMessageHandlingResult(TextMessages.SAVE_MESSAGE, new WaitForDocumentState());    //в этой строке мы переходим к состоянию ожидания документа
            case "type", "more", "TYPE", "MORE" -> new ChatMessageHandlingResult(TextMessages.EXEC_TYPE, new InitChatState());
            case "cd","chdir","CD","CHDIR" -> new ChatMessageHandlingResult(TextMessages.EXEC_CD, new InitChatState());
            case "dir","DIR" -> new ChatMessageHandlingResult(TextMessages.EXEC_DIR, new InitChatState());
            case "mkdir", "MKDIR", "md", "MD" -> new ChatMessageHandlingResult(TextMessages.EXEC_MKDIR, new InitChatState());
            case "ren", "rename", "REN", "RENAME" -> new ChatMessageHandlingResult(TextMessages.EXEC_REN, new InitChatState());
            //case "help ren", "help rename", "help REN", "help RENAME" -> new ChatMessageHandlingResult(TextMessages.EXEC_REN, new InitChatState());
            case "del", "erase", "DEL", "ERASE" -> new ChatMessageHandlingResult(TextMessages.EXEC_DEL, new InitChatState());
            case "start", "Start", "START" -> new ChatMessageHandlingResult(TextMessages.EXEC_START, new InitChatState());
            case "help", "HELP" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> new ChatMessageHandlingResult(TextMessages.WRONG_MESSAGE, this);
        };
    }
}
