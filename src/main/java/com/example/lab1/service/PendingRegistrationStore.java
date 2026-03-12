package com.example.lab1.service;

import com.example.lab1.model.PendingRegistration;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingRegistrationStore {

    private final Map<String, PendingRegistration> store = new ConcurrentHashMap<>();

    /** Сохранить запись (ключ = email) */
    public void save(PendingRegistration p) {
        store.put(p.getEmail(), p);
    }

    /** Получить запись по email */
    public PendingRegistration get(String email) {
        return store.get(email);
    }

    /** Удалить запись по email */
    public void remove(String email) {
        store.remove(email);
    }
}

