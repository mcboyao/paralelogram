package com.paralelogram.auth.service;

import com.paralelogram.auth.model.AccessToken;
import com.paralelogram.auth.model.TokenStatus;
import com.paralelogram.auth.model.UserCredentials;

public interface AuthService {

    AccessToken generateUserToken(UserCredentials credentials);

    AccessToken refreshUserToken(String refreshToken);

    TokenStatus validateLoggedInUserToken();

}
