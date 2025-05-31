package us.abbies.b.recordjson.tokens;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TokenizerTest {
    static Stream<Arguments> whitespaceTestCases() {
        return Stream.of(
                arguments("  \"a\"  ", List.of(Token.string("a", 1, 3))),
                arguments(" \t \"a\"\r\n\n \"a\" ", List.of(
                                Token.string("a", 1, 4),
                                Token.string("a", 3, 2)
                        )
                )
        );
    }

    static Stream<Arguments> stringTestCases() {
        return Stream.of(
                arguments("\"a\"", List.of(Token.string("a", 1, 1))),
                arguments("\"abc\"", List.of(Token.string("abc", 1, 1))),
                arguments("\"\\\"\"", List.of(Token.string("\"", 1, 1))),
                arguments("\"\\\\\"", List.of(Token.string("\\", 1, 1))),
                arguments("\"\\/\"", List.of(Token.string("/", 1, 1))),
                arguments("\"\\b\"", List.of(Token.string("\b", 1, 1))),
                arguments("\"\\f\"", List.of(Token.string("\f", 1, 1))),
                arguments("\"\\n\"", List.of(Token.string("\n", 1, 1))),
                arguments("\"\\r\"", List.of(Token.string("\r", 1, 1))),
                arguments("\"\\t\"", List.of(Token.string("\t", 1, 1))),
                arguments("\"\\q\"", List.of(new Token.Exception("Unrecognized escape sequence \\q", 1, 3).asErrorToken()))
        );
    }

    static Stream<Arguments> unicodeEscapeTestCases() {
        return Stream.of(
                arguments("\"\\u0000\"", List.of(Token.string("\u0000", 1, 1))),
                arguments("\"\\u000a\"", List.of(Token.string("\n", 1, 1))),
                arguments("\"\\u000A\"", List.of(Token.string("\n", 1, 1))),
                arguments("\"\\uD83D\\uDCA9\"\"\\uD83D\\uDCA9\"", List.of(
                        Token.string("\uD83D\uDCA9", 1, 1),
                        Token.string("\uD83D\uDCA9", 1, 15)
                )),
                arguments("\"\\uD83D\"", List.of(Token.string("\uD83D", 1, 1))),
                arguments("\"\\u0401\"", List.of(Token.string("Ё", 1, 1))),
                arguments("\"\\u040\"", List.of(new Token.Exception("Invalid character '\"' in Unicode escape", 1, 7).asErrorToken())),
                arguments("\"\\u040", List.of(new Token.Exception("Unexpected end of input", 1, 6).asErrorToken()))
        );
    }

    static Stream<Arguments> nullTestCases() {
        return Stream.of(
                arguments("null", List.of(Token.nullToken(1, 1))),
                arguments("nll", List.of(new Token.Exception("Unexpected character: l", 1, 2).asErrorToken())),
                arguments("nul", List.of(new Token.Exception("Unexpected end of input", 1, 3).asErrorToken()))
        );
    }

    static Stream<Arguments> booleanTestCases() {
        return Stream.of(
                arguments("false", List.of(Token.bool(false, 1, 1))),
                arguments("true", List.of(Token.bool(true, 1, 1))),
                arguments("flase", List.of(new Token.Exception("Unexpected character: l", 1, 2).asErrorToken())),
                arguments("ture", List.of(new Token.Exception("Unexpected character: u", 1, 2).asErrorToken())),
                arguments("f", List.of(new Token.Exception("Unexpected end of input", 1, 1).asErrorToken())),
                arguments("fals", List.of(new Token.Exception("Unexpected end of input", 1, 4).asErrorToken())),
                arguments("t", List.of(new Token.Exception("Unexpected end of input", 1, 1).asErrorToken())),
                arguments("tru", List.of(new Token.Exception("Unexpected end of input", 1, 3).asErrorToken()))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource({
            "booleanTestCases",
            "nullTestCases",
            "stringTestCases",
            "unicodeEscapeTestCases",
            "whitespaceTestCases"
    })
    public void runTest(String input, List<Token> expectedTokens) {
        var tokenizer = new Tokenizer(new StringReader(input));
        List<Token> actualTokens = new ArrayList<>();
        tokenizer.forEachRemaining(actualTokens::add);
        assertIterableEquals(expectedTokens, actualTokens);
    }
}
