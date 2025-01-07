package us.abbies.b.recordjson.tokens;

public record Token(TokenType type, Object value, int line, int column) {
}
