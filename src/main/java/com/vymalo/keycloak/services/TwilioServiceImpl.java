package com.vymalo.keycloak.services;


import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.userprofile.ValidationException;


import java.util.Map;

@Slf4j
public class TwilioServiceImpl implements TwilioService {

    private final String accountSid= "AC163c0c85cefaf8f3d7dafb1b90502144"; // Replace with your Twilio account SID
    private final String authToken = "08f39b21bfbec602a183b025e2a08549"; // Replace with your Twilio auth token
    private final String twilioPhoneNumber= "+19047340926"; // Replace with your Twilio phone number
//    private final MessageTemplateService messageTemplateService;


    @PostConstruct
    public void initializeTwilio() {
        log.info("Initializing Twilio with accountSid: {}", accountSid);
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized successfully.");
    }


    @Override
    public String sendOtp(String to, String otp) {


//        String body = messageTemplateService.formatMessage("sms/otp-sms-template.txt", Map.of("otpCode", otp));

        log.debug("Entering sendSms with toPhoneNumber: {}", to);
        try {
            MessageCreator messageCreator = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your OTP code is: " + otp); // Replace with your message template

            messageCreator.create();
            return "SMS sent successfully to {}"+ to;
        } catch (TwilioException e) {
            log.error("Error sending SMS to {}: {}", to, e.getMessage(), e);
            return "Error sending SMSto {}"+ to;
//            throw new ValidationException("Error sending SMS: " + e.getMessage());
        }
    }
}
