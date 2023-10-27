package ru.relex.SpringBabPog.service;

import org.telegram.telegrambots.meta.api.objects.Document;

public record ChatDocument(String name, long size, Document document) {
}
