package com.ahmad.hogwartsartifactsonline.security;

import com.ahmad.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.HogwartsUser;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.MyUserPrincipal;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;

    private final UserToUserDtoConverter userToUserDtoConverter;

    private final RedisCacheClient redisCacheClient;

    public AuthService(JwtProvider jwtProvider, UserToUserDtoConverter userToUserDtoConverter, RedisCacheClient redisCacheClient) {
        this.jwtProvider = jwtProvider;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.redisCacheClient = redisCacheClient;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {
        MyUserPrincipal principal = (MyUserPrincipal) authentication.getPrincipal();
        HogwartsUser hogwartsUser = principal.getHogwartsUser();
        UserDto userDto = userToUserDtoConverter.convert(principal.getHogwartsUser());

        String token = jwtProvider.createToken(authentication);

        // Save the token in Redis, key: whiteList:userId, value: token, Expire time is 2 hours.
        redisCacheClient.set("whiteList:" + hogwartsUser.getId(), token, 2, TimeUnit.HOURS);

        Map<String, Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo", userDto);
        loginResultMap.put("token", token);

        return loginResultMap;
    }
}
