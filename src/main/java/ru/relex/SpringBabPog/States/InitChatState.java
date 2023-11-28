package ru.relex.SpringBabPog.States;

import ru.relex.SpringBabPog.service.*;



public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message)     //реализация acceptMessage (начальное состояние)
    {
        return switch (message.getText()) {
            case "/start" -> new ChatMessageHandlingResult("Приветствую! Я бот, который сохраняет файлы на твой компьютер. \n" +
                    "Введи /help чтобы увидеть список команд", this);
            case "/savedocument" -> new ChatMessageHandlingResult(TextMessages.SAVE_MESSAGE, new WaitForDocumentState());    //в этой строке мы переходим к состоянию ожидания документа
            case "/getdocument" -> new ChatMessageHandlingResult(TextMessages.GET_MESSAGE, new NameOfDocumentState());
            case "/setpath" -> new ChatMessageHandlingResult(TextMessages.PATH_MESSAGE, new RepositoryPathState());
            case "/showdocuments" -> new ChatMessageHandlingResult(TextMessages.SHOW_MESSAGE, new InitChatState());
            case "/createnewpath" -> new ChatMessageHandlingResult(TextMessages.CREATE_PATH, new CreateFolderState());
            case "/renamedocument" -> new ChatMessageHandlingResult(TextMessages.RENAME_MESSAGE, new RenameChatState());
            case "/deletedocument" -> new ChatMessageHandlingResult(TextMessages.DELETE_MESSAGE, new DeleteDocumentState());
            case "/help" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> new ChatMessageHandlingResult(TextMessages.WRONG_MESSAGE, this);
        };
    }
}
