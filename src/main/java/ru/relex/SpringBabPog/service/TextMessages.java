package ru.relex.SpringBabPog.service;

public class TextMessages {
    public final static String DELETE_MESSAGE = "Команда DEL (ERASE) используется для удаления одного или нескольких файлов.\n" +
            "\n" +
            "Формат командной строки:\n" +
            "\n" +
            "DEL \n" +
            " [/P] [/F] [/S] [/Q] [/A[[:]атрибуты]] имена\n" +
            "\n" +
            "Или\n" +
            "\n" +
            "ERASE \n" +
            " [/P] [/F] [/S] [/Q] [/A[[:]атрибуты]] имена";
    public final static String EXEC_DELETE = "DELETE";
    public final static String PATH_MESSAGE = "Команды CD и CHDIR используется для просмотра или изменения пути текущего каталога " +
            "CHDIR \n" +
            " [/D] [диск:][путь]\n" +
            "\n" +
            "CD \n" +
            "[/D] [диск:][путь]\n" +
            "\n" +
            "CHDIR \n " +
            "[..]\n" +
            "\n" +
            "CD \n" +
            "[..]";
    public final static String EXEC_PATH = "SET_PATH";
    public final static String EXEC_CREATE_PATH = "SET_PATH_CREATE";
    public final static String SAVE_MESSAGE = "SAVE - сохранение отправленного файла" +
            "SAVE" +
            "имя_файла";
    public final static String EXEC_SAVE = "SAVE";
    public final static String GET_MESSAGE = "TYPE (MORE) – вывод на экран файла" +
            "Формат командной строки:\n" +
            "\n" +
            "TYPE \n" +
            "[диск:][путь]имя_файла";
    public final static String EXEC_GET = "GET";
    private static String PATH_TO_FILE;
    public final static String SHOW_MESSAGE = "Каталог: ";
    public final static String RENAME_MESSAGE = "Команда RENAME имеет синоним REN и предназначена для переименования файлов и каталогов.\n" +
            "\n" +
            "Формат командной строки:\n" +
            "\n" +
            "RENAME \n" +
            "[диск:][путь]имя_файла1 : имя_файла2\n" +
            "\n" +
            "REN \n" +
            "[диск:][путь]имя_файла1 : имя_файла2\n" +
            "\n" +
            "Параметры командной строки:\n" +
            "\n" +
            "диск: - диск на котором находится исходный файл или каталог;\n" +
            "\n" +
            "путь - путь к исходному файлу или каталогу;\n" +
            "\n" +
            "имя_файла1 - исходное имя файла или каталога;\n" +
            "\n" +
            "имя_файла2 - новое имя файла или каталога;";
    public final static String CREATE_PATH = "mkdir <путь к папке>\n" +
            "\n" +
            "Например:\n" +
            "\n" +
            "mkdir C:\\Users\\Имя_Пользователя\\Новая_Папка";
    public final static String EXEC_RENAME = "RENAME";

    //public final static String EXEC_SHOW = "SHOW";
    public final static String WRONG_MESSAGE = "Слышь, нормально общайся! Неправильная команда";
}
