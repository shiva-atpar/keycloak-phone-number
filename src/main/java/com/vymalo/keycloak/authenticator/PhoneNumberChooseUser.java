package com.vymalo.keycloak.authenticator;

import com.vymalo.keycloak.constants.ConfigKey;
import com.vymalo.keycloak.constants.PhoneKey;
import com.vymalo.keycloak.constants.PhoneNumberHelper;
import com.vymalo.keycloak.constants.Utils;
import com.vymalo.keycloak.services.SmsService;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.UserProvider;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import java.util.Collections;

@JBossLog
@NoArgsConstructor
public class PhoneNumberChooseUser extends AbstractPhoneNumberAuthenticator {

    public static final String PROVIDER_ID = "phone-number-choose-user";
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final var event = context.getEvent();
        final var authenticationSession = context.getAuthenticationSession();
        final var phoneNumber = authenticationSession.getAuthNote(PhoneKey.ATTEMPTED_PHONE_NUMBER);

        final var realm = context.getRealm();
        final var attrName = Utils
                .getEnv(ConfigKey.USER_PHONE_ATTRIBUTE_NAME, PhoneNumberHelper.DEFAULT_PHONE_KEY_NAME);

        var user = context.getUser();

        if (user != null && !user.isEnabled()) {
            event.detail("phone_number", phoneNumber)
                    .user(user)
                    .error(Errors.USER_DISABLED);
            context.clearUser();
            context.resetFlow();
            return;
        }

        if (user != null && user.isEnabled()) {
            user.setAttribute(attrName, Collections.singletonList(phoneNumber));
        } else {
            UserProvider userProvider = context.getSession().users();
            final var users = userProvider
                    .searchForUserByUserAttributeStream(realm, attrName, phoneNumber)
                    .toList();

            if (users.isEmpty() && user == null) {
                //final var newUser = userProvider.addUser(realm, phoneNumber);
                //newUser.setAttribute(attrName, Collections.singletonList(phoneNumber));
                //newUser.setEnabled(true);
                //user = newUser;
                final var challenge = context.form().setUser(user)
                        .setAttribute("phoneNumber", phoneNumber)
                        //.setAttribute("countries", SmsService.getAllCountries())
                        .createForm("register-user-phone-number.ftl");
                context.challenge(challenge);
            } else {
                user = users.get(0);
                context.success();
            }
        }



        //context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        final var formData = context.getHttpRequest().getDecodedFormParameters();
        final var attrName = Utils
                .getEnv(ConfigKey.USERNAME_ATTRIBUTE_NAME, PhoneNumberHelper.USERNAME_KEY_NAME);
        var user = context.getUser();
        final var event = context.getEvent();
        final var authenticationSession = context.getAuthenticationSession();
        final var phoneNumber = authenticationSession.getAuthNote(PhoneKey.ATTEMPTED_PHONE_NUMBER);
        if (user != null && !user.isEnabled()) {
            event.detail("phone_number", phoneNumber)
                    .user(user)
                    .error(Errors.USER_DISABLED);
            context.clearUser();
            context.resetFlow();
            return;
        }


        final var firstName = formData.getFirst("firstName");
        final var lastName = formData.getFirst("lastName");
        final var email = formData.getFirst("email");
        final var username = formData.getFirst("username");
        final var realm = context.getRealm();
        UserProvider userProvider = context.getSession().users();
        final var users = userProvider
                .searchForUserByUserAttributeStream(realm, attrName, phoneNumber)
                .toList();
        user = userProvider.getUserByUsername(realm,username);
        if (users.isEmpty()  || user==null) {
            final var newUser = userProvider.addUser(realm, username);
            newUser.setAttribute(attrName, Collections.singletonList(username));
            newUser.setEnabled(true);
            context.setUser(newUser);
            context.success();
        }else{

            final var challenge = context.form().setUser(user)
                    .setAttribute("phoneNumber", phoneNumber)
                    .createForm("register-user-phone-number.ftl");
            context.challenge(challenge);
        }

        final var cancel = formData.getFirst("cancel");
        if (cancel != null) {
            context.resetFlow();
        }
        context.success();
    }

    @Override
    public String getDisplayType() {
        return "SMS -3 Choose user by Phone number";
    }

    @Override
    public String getReferenceCategory() {
        return "phone-choose";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Choose a user, by his/her phone number, to reset credentials for";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
