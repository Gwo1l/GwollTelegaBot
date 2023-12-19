package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.*;



public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult acceptMessage(ChatMessage message)
    {
        return switch (message.getText()) {
            case "save", "SAVE" -> new ChatMessageHandlingResult(TextMessages.SAVE_STATE, new WaitForDocumentState());
            case "type", "more", "TYPE", "MORE" -> new ChatMessageHandlingResult(TextMessages.EXEC_TYPE, new InitChatState());
            case "cd","chdir","CD","CHDIR" -> new ChatMessageHandlingResult(TextMessages.EXEC_CD, new InitChatState());
            case "dir","DIR" -> new ChatMessageHandlingResult(TextMessages.EXEC_DIR, new InitChatState());
            case "mkdir", "MKDIR", "md", "MD" -> new ChatMessageHandlingResult(TextMessages.EXEC_MKDIR, new InitChatState());
            case "ren", "rename", "REN", "RENAME" -> new ChatMessageHandlingResult(TextMessages.EXEC_REN, new InitChatState());
            //case "help ren", "help rename", "help REN", "help RENAME" -> new ChatMessageHandlingResult(TextMessages.EXEC_REN, new InitChatState());
            case "del", "erase", "DEL", "ERASE" -> new ChatMessageHandlingResult(TextMessages.EXEC_DEL, new InitChatState());
            case "start", "Start", "START" -> new ChatMessageHandlingResult(TextMessages.EXEC_START, new InitChatState());
            case "help", "HELP" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> {
                // Check if text is "help" and process fileName
                if ("help".equalsIgnoreCase(message.getText()) && message.getFileName() != null) {
                    switch (message.getFileName().toLowerCase()) {
                        case "save":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_SAVE, this);
                        case "type", "more":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_TYPE, this);
                        case "cd","chdir":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_CD, this);
                        case "dir":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_DIR, this);
                        case "md", "mkdir":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_MKDIR, this);
                        case "ren", "rename":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_REN, this);
                        case "del", "erase":
                            yield new ChatMessageHandlingResult(TextMessages.HELP_DEL, this);
                            // Add other cases as needed
                        default:
                            yield new ChatMessageHandlingResult("Unknown command in help.", this);
                    }
                } else if ("help".equalsIgnoreCase(message.getText()) && message.getFileName() == null) {
                    yield new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);
                } else {
                    yield new ChatMessageHandlingResult(TextMessages.WRONG_MESSAGE, this);
                }
            }        };
    }
}
