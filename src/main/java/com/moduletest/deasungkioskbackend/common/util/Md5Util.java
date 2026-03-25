package com.moduletest.deasungkioskbackend.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

    public static String md5(String input) {
        try {
            // MD5 MessageDigest 생성
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 문자열을 byte 배열로 변환 후 해시 생성
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // byte → hex 문자열 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString(); // md5 결과 반환

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
