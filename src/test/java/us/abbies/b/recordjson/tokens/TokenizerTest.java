package us.abbies.b.recordjson.tokens;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TokenizerTest {
    static Stream<Arguments> nullTestCases() {
        return Stream.of(
                arguments("null", List.of(new Token(TokenType.LIT_NULL, null, 1, 1))),
                arguments("nll", List.of(new Token(TokenType.ERROR, "Unexpected character: l", 1, 2))),
                arguments("nul", List.of(new Token(TokenType.ERROR, "Unexpected end of input", 1, 3)))
        );
    }

    static Stream<Arguments> booleanTestCases() {
        return Stream.of(
                arguments("false", List.of(new Token(TokenType.LIT_BOOL, false, 1, 1))),
                arguments("true", List.of(new Token(TokenType.LIT_BOOL, true, 1, 1))),
                arguments("flase", List.of(new Token(TokenType.ERROR, "Unexpected character: l", 1, 2))),
                arguments("ture", List.of(new Token(TokenType.ERROR, "Unexpected character: u", 1, 2))),
                arguments("f", List.of(new Token(TokenType.ERROR, "Unexpected end of input", 1, 1))),
                arguments("fals", List.of(new Token(TokenType.ERROR, "Unexpected end of input", 1, 4))),
                arguments("t", List.of(new Token(TokenType.ERROR, "Unexpected end of input", 1, 1))),
                arguments("tru", List.of(new Token(TokenType.ERROR, "Unexpected end of input", 1, 3)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource({"booleanTestCases", "nullTestCases"})
    public void runTests(String input, List<Token> expectedTokens) {
        var tokenizer = new Tokenizer(new StringReader(input));
        List<Token> actualTokens = new ArrayList<>();
        tokenizer.forEachRemaining(actualTokens::add);
        assertIterableEquals(expectedTokens, actualTokens);
    }
}
