package us.abbies.b.recordjson.tokens;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final Object value;
    private final int line;
    private final int column;

    private Token(TokenType type, Object value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType type() {
        return type;
    }

    public Object value() {
        return value;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public static Token objStart(int line, int column) {
        return new Token(TokenType.OBJ_START, null, line, column);
    }

    public static Token objEnd(int line, int column) {
        return new Token(TokenType.OBJ_END, null, line, column);
    }

    public static Token objNameSep(int line, int column) {
        return new Token(TokenType.OBJ_NAME_SEP, null, line, column);
    }

    public static Token objValSep(int line, int column) {
        return new Token(TokenType.OBJ_VAL_SEP, null, line, column);
    }

    public static Token arrayStart(int line, int column) {
        return new Token(TokenType.ARRAY_START, null, line, column);
    }

    public static Token arrayEnd(int line, int column) {
        return new Token(TokenType.ARRAY_END, null, line, column);
    }

    public static Token bool(boolean value, int line, int column) {
        return new Token(TokenType.LIT_BOOL, value, line, column);
    }

    public static Token nullToken(int line, int column) {
        return new Token(TokenType.LIT_NULL, null, line, column);
    }

    public static Token string(String value, int line, int column) {
        return new Token(TokenType.LIT_STR, value, line, column);
    }

    public static Token longToken(long value, int line, int column) {
        return new Token(TokenType.LIT_LONG, value, line, column);
    }

    public static Token doubleToken(double value, int line, int column) {
        return new Token(TokenType.LIT_DOUBLE, value, line, column);
    }

    public static class Exception extends RuntimeException {
        private final String message;
        private final int line;
        private final int column;

        public Exception(String message, Throwable cause, int line, int column) {
            super(message, cause);
            this.message = message;
            this.line = line;
            this.column = column;
        }

        public Exception(String message, int line, int column) {
            super(message);
            this.message = message;
            this.line = line;
            this.column = column;
        }

        public Token asErrorToken() {
            return new Token(TokenType.ERROR, message, line, column);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Token) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.value, that.value) &&
                this.line == that.line &&
                this.column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, line, column);
    }

    @Override
    public String toString() {
        return "Token[" +
                "type=" + type + ", " +
                "value=" + value + ", " +
                "line=" + line + ", " +
                "column=" + column + ']';
    }

}
