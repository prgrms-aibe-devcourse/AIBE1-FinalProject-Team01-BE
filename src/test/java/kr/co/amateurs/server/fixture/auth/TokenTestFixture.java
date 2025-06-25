package kr.co.amateurs.server.fixture.auth;

import kr.co.amateurs.server.fixture.common.UserTestFixture;

public class TokenTestFixture {

    public static final Long ACCESS_TOKEN_EXPIRATION = 3600000L;
    public static final Long REFRESH_TOKEN_EXPIRATION = 1209600000L;

    public static final String TOKEN_TYPE = "Bearer";

    public static final String INVALID_TOKEN = "invalid.token.format";
    public static final String EMPTY_TOKEN = "";
    public static final String NULL_TOKEN = null;

    public static String getTestEmail() {
        return UserTestFixture.DEFAULT_EMAIL;
    }

    public static String getNonExistentEmail() {
        return "notfound@test.com";
    }

    public static String getTestPassword() {
        return "password123";
    }

    public static String getWrongPassword() {
        return "wrongpassword";
    }
}