package com.monstertamers.auth.internal.application.mail;

public interface EmailSender {

    void send(String to, String subject, String body);
}
