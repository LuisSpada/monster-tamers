package com.monstertamers.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailDeliveryProperties {

    private Mode mode = Mode.LOGGING;
    private String from;
    private final Smtp smtp = new Smtp();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public enum Mode {
        LOGGING,
        SMTP
    }

    public static class Smtp {

        private String host;
        private int port = 587;
        private String username;
        private String password;
        private boolean auth = true;
        private boolean starttls = true;
        private int connectionTimeoutMs = 5000;
        private int readTimeoutMs = 5000;
        private int writeTimeoutMs = 5000;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAuth() {
            return auth;
        }

        public void setAuth(boolean auth) {
            this.auth = auth;
        }

        public boolean isStarttls() {
            return starttls;
        }

        public void setStarttls(boolean starttls) {
            this.starttls = starttls;
        }

        public int getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public void setConnectionTimeoutMs(
                int connectionTimeoutMs
        ) {
            this.connectionTimeoutMs =
                    connectionTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public int getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        public void setWriteTimeoutMs(
                int writeTimeoutMs
        ) {
            this.writeTimeoutMs = writeTimeoutMs;
        }
    }
}