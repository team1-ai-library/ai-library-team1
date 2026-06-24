package com.nhnacademy.ailibraryteam1.feedback.util;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class HashUtils {
    private static final String SALT = "ai-library-telegram-salt-2026";
    private static final String ALGORITHM = "SHA-256";

    public static String hashChatId(long chatId) {
        try {
            String input = chatId + SALT;
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.HASH_ALGORITHM_NOT_FOUND);
        }
    }
}
