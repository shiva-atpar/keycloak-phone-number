package com.vymalo.keycloak.services;

public interface TwilioService {


    public abstract String sendOtp(String to, String otp);
}
