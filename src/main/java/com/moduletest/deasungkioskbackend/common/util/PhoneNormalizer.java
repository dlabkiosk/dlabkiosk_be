package com.moduletest.deasungkioskbackend.common.util;

/**
 * DSA 응답의 전화번호 정규화 유틸.
 * Why: DSA 일부 응답에 전각 숫자(０-９, U+FF10~U+FF19)/전각 하이픈/공백이 섞여 옴.
 * 기존 `replaceAll("[^0-9]","")`는 전각 숫자도 함께 제거해버려 phone이 빈 문자열이 됨.
 */
public final class PhoneNormalizer {

    private static final char FULLWIDTH_DIGIT_START = '０';
    private static final char FULLWIDTH_DIGIT_END = '９';
    private static final int FULLWIDTH_TO_HALFWIDTH_OFFSET = 0xFEE0;

    private PhoneNormalizer() {
    }

    /**
     * 숫자만 남긴다. 전각 숫자는 반각으로 변환 후 채택, 그 외(하이픈/공백/한글 등)는 모두 제거.
     */
    public static String normalizeDigits(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            } else if (c >= FULLWIDTH_DIGIT_START && c <= FULLWIDTH_DIGIT_END) {
                sb.append((char) (c - FULLWIDTH_TO_HALFWIDTH_OFFSET));
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /**
     * 표시용 정규화. 전각 → 반각만 변환하고 하이픈/공백 등 포맷 문자는 유지.
     * 검색이 아닌 화면 표시(예: 학부모 번호)에 사용.
     */
    public static String normalizeFullwidth(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '！' && c <= '～') {
                sb.append((char) (c - FULLWIDTH_TO_HALFWIDTH_OFFSET));
            } else if (c == '　') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 끝 N자만 잘라낸다. null/짧은 문자열 그대로 반환.
     */
    public static String last(String digits, int n) {
        if (digits == null) {
            return null;
        }
        return digits.length() <= n ? digits : digits.substring(digits.length() - n);
    }
}
