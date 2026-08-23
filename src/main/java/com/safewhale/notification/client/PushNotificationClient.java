package com.safewhale.notification.client;

public interface PushNotificationClient {
    boolean send(String pushToken, String title, String message);
}
