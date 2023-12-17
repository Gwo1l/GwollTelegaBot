package ru.relex.SpringBabPog.states;

import ru.relex.SpringBabPog.service.*;



public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message)     //реализация acceptMessage (начальное состояние)
    {
        return switch (message.getText()) {
            case "save", "SAVE" -> new ChatMessageHandlingResult(TextMessages.SAVE_MESSAGE, new WaitForDocumentState());    //в этой строке мы переходим к состоянию ожидания документа
            case "type", "more", "TYPE", "MORE" -> new ChatMessageHandlingResult(TextMessages.GET_MESSAGE, new NameOfDocumentState());
            case "cd","chdir","CD","CHDIR" -> new ChatMessageHandlingResult(TextMessages.PATH_MESSAGE, new RepositoryPathState());
            case "dir","DIR" -> new ChatMessageHandlingResult(TextMessages.SHOW_MESSAGE, new InitChatState());
            case "mkdir", "MKDIR" -> new ChatMessageHandlingResult(TextMessages.CREATE_PATH, new CreateFolderState());
            case "ren", "rename", "REN", "RENAME" -> new ChatMessageHandlingResult(TextMessages.RENAME_MESSAGE, new RenameChatState());
            case "del", "erase", "DEL", "ERASE" -> new ChatMessageHandlingResult(TextMessages.DELETE_MESSAGE, new DeleteDocumentState());
            case "help", "HELP" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> new ChatMessageHandlingResult(TextMessages.WRONG_MESSAGE, this);
        };
    }
}
