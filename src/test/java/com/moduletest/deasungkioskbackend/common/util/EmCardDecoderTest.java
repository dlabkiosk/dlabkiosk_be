package com.moduletest.deasungkioskbackend.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class EmCardDecoderTest {

    @Test
    void decodesKnownCardHexToDecimal() {
        assertThat(EmCardDecoder.decode("478630F4")).contains("22624844");
        assertThat(EmCardDecoder.decode("A33E6CA3")).contains("19731798");
    }

    @Test
    void stripsStxAndEtxFraming() {
        assertThat(EmCardDecoder.decode("\u0002478630F4\u0003")).contains("22624844");
    }

    @Test
    void acceptsLowercaseHex() {
        assertThat(EmCardDecoder.decode("478630f4")).contains("22624844");
    }

    @Test
    void ignoresCharactersBeyondSixthHex() {
        assertThat(EmCardDecoder.decode("478630")).contains("22624844");
        assertThat(EmCardDecoder.decode("478630FFFF")).contains("22624844");
    }

    @Test
    void returnsEmptyWhenInputIsNull() {
        assertThat(EmCardDecoder.decode(null)).isEmpty();
    }

    @Test
    void returnsEmptyWhenInputTooShort() {
        assertThat(EmCardDecoder.decode("12345")).isEmpty();
    }

    @Test
    void returnsEmptyWhenInputHasNonHexCharacters() {
        assertThat(EmCardDecoder.decode("ZZZZZZ")).isEmpty();
        assertThat(EmCardDecoder.decode("478G30F4")).isEmpty();
    }

    @Test
    void containsHexLetterDetectsRawHexCorrectly() {
        assertThat(EmCardDecoder.containsHexLetter("478630F4")).isTrue();
        assertThat(EmCardDecoder.containsHexLetter("a33e6ca3")).isTrue();
        assertThat(EmCardDecoder.containsHexLetter("22624844")).isFalse();
        assertThat(EmCardDecoder.containsHexLetter("12345678")).isFalse();
        assertThat(EmCardDecoder.containsHexLetter(null)).isFalse();
    }

    @Test
    void pureNumericEightDigitInputDecodesButResultMayDiffer() {
        Optional<String> decoded = EmCardDecoder.decode("12345678");
        assertThat(decoded).isPresent();
        assertThat(decoded.get()).isNotEqualTo("12345678");
    }
}
