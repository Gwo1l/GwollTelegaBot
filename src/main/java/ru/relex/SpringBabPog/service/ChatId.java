package ru.relex.SpringBabPog.service;

public final class ChatId {     //просто класс с описывающий чат айди
    private final long id;

    public ChatId(long id) {
        this.id = id;
    }

    public long getValue(){
        return id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var anotherId = (ChatId) o;
        return anotherId.getValue() == id;
    }
}
