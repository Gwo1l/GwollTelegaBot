package ru.relex.SpringBabPog.service;

public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message)     //реализация acceptMessage (начальное состояние)
    {
        return switch (message.getText()) {
            case "/start" -> new ChatMessageHandlingResult("Отправь свой документ", new WaitForDocumentState());    //в этой строке мы переходим к состоянию ожидания документа
            case "/mydocuments" -> new ChatMessageHandlingResult("Введите имя файла", new NameOfDocumentState());
            case "/help" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> new ChatMessageHandlingResult("Слышь, нормально общайся! Неправильная команда", this);
        };
    }
}
