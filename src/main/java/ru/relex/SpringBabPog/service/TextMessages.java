package ru.relex.SpringBabPog.service;

public class TextMessages {
    public final static String EXEC_DEL = "del";

    public final static String HELP_DEL = "Удаление файла в текущей директории.\n" +
            "\n" +
            "DEL" +
            " [имя файла]\n" +
            "ERASE" +
            " [имя файла]";

    public final static String EXEC_CD = "cd";

    public final static String SAVE_STATE = "отправьте файл";
    public final static String HELP_CD = "Изменение пути текущего каталога." +
            //"Формат командной строки:" +
            "CD" +
            "[диск:][путь]\n" +
            //"\nПараметры командной строки:\n" +
            "\n" +
            "диск: - диск на котором находится исходный файл или каталог;\n" +
            "\n" +
            "путь - путь к исходному файлу или каталогу;\n" +
            "\nНапример:\n" +
            "C:\\Users\\Имя_Пользователя\\Папка";
    public final static String EXEC_PATH = "SET_PATH";
    public final static String EXEC_CREATE_PATH = "SET_PATH_CREATE";
    public final static String HELP_SAVE = "Сохранение отправленного файла.\n" +
            //"\nИспользование: \n" +
            "save <файл>";
    public final static String EXEC_SAVE = "SAVE";
    public final static String EXEC_TYPE = "TYPE";
    public final static String HELP_TYPE = "Вывод файла" +
            //"Формат командной строки:\n" +
            "\n" +
            "TYPE \n" +
            "[диск:][путь]имя_файла";
    public final static String EXEC_GET = "GET";
    public final static String HELP = "help";
    private static String PATH_TO_FILE;
    public final static String EXEC_DIR = "Каталог: ";
    public final static String HELP_DIR = "Получение списка файлов и папок в указанной директории.\n" +
            //"\nИспользование: \n" +
            "dir - выводит список файлов и папок в текущей директории\n";
    public final static String EXEC_START = "старт";
    public final static String EXEC_REN = "REN";
    public final static String HELP_REN = "Переименование файлов и каталогов.\n" +
            "\n" +
            //"Формат командной строки:\n" +
            "\n" +
            "RENAME" +
            "[диск:][путь]имя_файла1 : имя_файла2\n" +
            "REN" +
            "[диск:][путь]имя_файла1 : имя_файла2\n" +
            //"Параметры командной строки:\n" +
            "диск: - диск на котором находится исходный файл или каталог;\n" +
            "путь - путь к исходному файлу или каталогу;\n" +
            "имя_файла1 - исходное имя файла или каталога;\n" +
            "имя_файла2 - новое имя файла или каталога;";
    public final static String EXEC_MKDIR = "MKDIR";
    public final static String HELP_MKDIR =
            "Создание новой папки (директории).\n" +
                    //"\nИспользование:\n" +
                    "mkdir <путь к папке>\n" +
                    "\n" +
                    "Например:\n" +
                    "mkdir C:\\Users\\Имя_Пользователя\\Новая_Папка";
    public final static String EXEC_RENAME = "RENAME";

    //public final static String EXEC_SHOW = "SHOW";
    public final static String WRONG_MESSAGE = "Не является внутренней или внешней командой, исполняемой программой или пакетным файлом.";
}
