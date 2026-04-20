package com.moduletest.deasungkioskbackend.common.util;

import java.util.Optional;

/**
 * DSA 제공 RFID 카드(EctReader) 디코딩 유틸.
 * 리더기 raw hex 8자 중 앞 6자를 바이트 스왑 + 니블 비트반전 후 3+5 10진수 포맷으로 변환한다.
 * 예) "478630F4" → "22624844"
 */
public final class EmCardDecoder {

    private static final int REQUIRED_HEX_LENGTH = 6;
    private static final char STX = '\u0002';
    private static final char ETX = '\u0003';

    private static final int[] NIBBLE_BIT_REVERSE = new int[16];

    static {
        NIBBLE_BIT_REVERSE[0x0] = 0x0;
        NIBBLE_BIT_REVERSE[0x1] = 0x8;
        NIBBLE_BIT_REVERSE[0x2] = 0x4;
        NIBBLE_BIT_REVERSE[0x3] = 0xC;
        NIBBLE_BIT_REVERSE[0x4] = 0x2;
        NIBBLE_BIT_REVERSE[0x5] = 0xA;
        NIBBLE_BIT_REVERSE[0x6] = 0x6;
        NIBBLE_BIT_REVERSE[0x7] = 0xE;
        NIBBLE_BIT_REVERSE[0x8] = 0x1;
        NIBBLE_BIT_REVERSE[0x9] = 0x9;
        NIBBLE_BIT_REVERSE[0xA] = 0x5;
        NIBBLE_BIT_REVERSE[0xB] = 0xD;
        NIBBLE_BIT_REVERSE[0xC] = 0x3;
        NIBBLE_BIT_REVERSE[0xD] = 0xB;
        NIBBLE_BIT_REVERSE[0xE] = 0x7;
        NIBBLE_BIT_REVERSE[0xF] = 0xF;
    }

    private EmCardDecoder() {
    }

    /**
     * 리더기 raw hex 문자열을 DB 저장용 8자리 10진수로 변환한다.
     * STX/ETX는 제거하고, 앞 6자만 사용한다. (DSA EctReader 동작과 동일)
     */
    public static Optional<String> decode(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String stripped = raw
            .replace(String.valueOf(STX), "")
            .replace(String.valueOf(ETX), "")
            .trim()
            .toUpperCase();
        if (stripped.length() < REQUIRED_HEX_LENGTH) {
            return Optional.empty();
        }
        if (!isHex(stripped)) {
            return Optional.empty();
        }
        try {
            String swapped = stripped.substring(4, 6)
                + stripped.substring(2, 4)
                + stripped.substring(0, 2);
            StringBuilder reversed = new StringBuilder(REQUIRED_HEX_LENGTH);
            for (int i = REQUIRED_HEX_LENGTH - 1; i >= 0; i--) {
                int digit = Character.digit(swapped.charAt(i), 16);
                reversed.append(Integer.toHexString(NIBBLE_BIT_REVERSE[digit]).toUpperCase());
            }
            long facility = Long.parseLong(reversed.substring(0, 2), 16);
            long card = Long.parseLong(reversed.substring(2, 6), 16);
            return Optional.of(String.format("%03d%05d", facility, card));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * 입력이 hex 문자(A-F) 포함 = raw 카드 데이터 가능성 높음.
     * 이미 디코딩된 10진수(전부 숫자)와 구분하는 힌트로 사용한다.
     */
    public static boolean containsHexLetter(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'F') {
                return true;
            }
        }
        return false;
    }

    private static boolean isHex(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean isDigit = c >= '0' && c <= '9';
            boolean isUpperHex = c >= 'A' && c <= 'F';
            if (!isDigit && !isUpperHex) {
                return false;
            }
        }
        return true;
    }
}
