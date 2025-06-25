package kr.co.amateurs.server.fixture.auth;

import kr.co.amateurs.server.fixture.common.UserTestFixture;

public class TokenTestFixture {

    public static final Long ACCESS_TOKEN_EXPIRATION = 3600000L;
    public static final Long REFRESH_TOKEN_EXPIRATION = 1209600000L;

    public static String getTestEmail() {
        return UserTestFixture.DEFAULT_EMAIL;
    }

    public static String getAnotherTestEmail() {
        return "another@test.com";
    }
}
