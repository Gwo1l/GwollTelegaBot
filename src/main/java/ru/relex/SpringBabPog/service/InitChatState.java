package ru.relex.SpringBabPog.service;

public final class InitChatState extends ChatState {
    @Override
    public ChatMessageHandlingResult AcceptMessage(ChatMessage message)     //реализация acceptMessage (начальное состояние)
    {
        return switch (message.getText()) {
            case "/start" -> new ChatMessageHandlingResult("Приветствую! Я бот, который сохраняет файлы на твой компьютер. \n" +
                    "Введи /help чтобы увидеть список команд", this);
            case "/savedocument" -> new ChatMessageHandlingResult("Отправь свой документ", new WaitForDocumentState());    //в этой строке мы переходим к состоянию ожидания документа
            case "/getdocument" -> new ChatMessageHandlingResult("Введи имя файла (вместе с расширением)", new NameOfDocumentState());
            case "/setpath" -> new ChatMessageHandlingResult("Введи путь до репозитория", new RepositoryPathState());
            case "/help" -> new ChatMessageHandlingResult(TelegramBot.HELP_TEXT, this);     //this означает что мы передаем то же самое состояние (ну или не меняем начальное состояние)
            default -> new ChatMessageHandlingResult("Слышь, нормально общайся! Неправильная команда", this);
        };
    }
}
