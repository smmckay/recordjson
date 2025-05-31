package us.abbies.b.recordjson.tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Tokenizer implements Iterator<Token> {
    private final Reader input;
    private int line = 1;
    private int column;
    private Token onDeck;
    private int peeked;
    private boolean hitError;

    Tokenizer(Reader input) {
        this.input = Objects.requireNonNull(input, "input must not be null");
    }

    @Override
    public boolean hasNext() {
        if (onDeck != null) {
            return true;
        } else {
            tryReadToken();
        }
        return onDeck != null;
    }

    @Override
    public Token next() {
        if (onDeck == null) {
            tryReadToken();
        }
        if (onDeck == null) {
            throw new NoSuchElementException();
        }

        Token result = onDeck;
        onDeck = null;
        return result;
    }

    private void tryReadToken() {
        if (onDeck != null) {
            throw new AssertionError("tryReadToken called with token on deck");
        } else if (hitError) {
            return;
        }

        try {
            int c = discardWhitespace();
            onDeck = switch (c) {
                case '{' -> Token.objStart(line, column);
                case '}' -> Token.objEnd(line, column);
                case ':' -> Token.objNameSep(line, column);
                case ',' -> Token.objValSep(line, column);
                case '[' -> Token.arrayStart(line, column);
                case ']' -> Token.arrayEnd(line, column);
                case '"' -> readString(column);
                case 'f' -> expect("alse", Token.bool(false, line, column));
                case 'n' -> expect("ull", Token.nullToken(line, column));
                case 't' -> expect("rue", Token.bool(true, line, column));
                case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> readNumber(column, c);
                case -1 -> null;
                default -> throw new Token.Exception("Unrecognized character: " + Character.toString(c), line, column);
            };
        } catch (Token.Exception e) {
            hitError = true;
            onDeck = e.asErrorToken();
        } catch (RuntimeException e) {
            hitError = true;
            onDeck = new Token.Exception(e.getMessage(), e, line, column).asErrorToken();
        }
    }

    private Token readString(int startingColumn) {
        StringBuilder result = new StringBuilder();
        while (true) {
            int c = read();
            if (c == '\\') {
                result.append(readCharEscape());
            } else if (c == '"') {
                return Token.string(result.toString(), line, startingColumn);
            } else if (c <= 0x1F) {
                throw new Token.Exception("Control characters not allowed inside strings", line, column);
            } else {
                result.append((char) c);
            }
        }
    }

    private char readCharEscape() {
        int c = read();
        return switch (c) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> (char) (readHexDigit() << 12 | readHexDigit() << 8 | readHexDigit() << 4 | readHexDigit());
            default -> throw new Token.Exception("Unrecognized escape sequence \\" + (char) c, line, column);
        };
    }

    private int readHexDigit() {
        int c = read();
        int result = Character.digit(c, 16);
        if (result == -1) {
            throw new Token.Exception("Invalid character '" + (char) c + "' in Unicode escape", line, column);
        }
        return result;
    }

    private Token readNumber(int startingColumn, int firstChar) {
        StringBuilder buf = new StringBuilder();
        int minusFactor = 1;
        if (firstChar == '-') {
            minusFactor = -1;
        } else {
            putBack(firstChar);
        }

        boolean isLong = true;
        int c = accumulateDigits(buf);

        if (c == '.') {
            // fractional part
            isLong = false;
            buf.append((char) c);
            c = accumulateDigits(buf);
        }

        if (c == 'e' || c == 'E') {
            // exponent
            isLong = false;
            buf.append((char) c);
            c = accumulateDigits(buf);
        }

        putBack(c);

        try {
            if (isLong) {
                return Token.longToken(minusFactor * Long.parseUnsignedLong(buf, 0, buf.length(), 10), line, startingColumn);
            } else {
                return Token.doubleToken(minusFactor * Double.parseDouble(buf.toString()), line, startingColumn);
            }
        } catch (NumberFormatException e) {
            return new Token.Exception("Invalid numeric literal", e, line, startingColumn).asErrorToken();
        }
    }

    private int accumulateDigits(StringBuilder buf) {
        int i, c;
        for (c = nextChar(true), i = 0; c >= '0' && c <= '9'; c = nextChar(true), i++) {
            buf.append((char) c);
        }
        if (i == 0) {
            throw new Token.Exception("Expected digits in numeric literal", line, column);
        }
        return c;
    }

    private Token expect(String remaining, Token successToken) {
        for (char expected : remaining.toCharArray()) {
            int actual = read();
            if (expected != actual) {
                throw new Token.Exception("Unexpected character: " + Character.toString(actual), line, column);
            }
        }
        return successToken;
    }

    private int discardWhitespace() {
        int result;
        do {
            result = nextChar(true);
            if (result == 0x0A) {
                line++;
                column = 0;
            }
        } while (isWhitespace(result));
        return result;
    }

    private boolean isWhitespace(int c) {
        return c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D;
    }

    private int read() {
        return nextChar(false);
    }

    private int nextChar(boolean eofAllowed) {
        if (peeked != 0) {
            int result = peeked;
            peeked = 0;
            return result;
        }

        int result;
        try {
            result = input.read();
        } catch (IOException e) {
            throw new Token.Exception(e.getMessage(), e, line, column);
        }

        if (result == -1 && !eofAllowed) {
            throw new Token.Exception("Unexpected end of input", line, column);
        }

        column++;
        return result;
    }

    private void putBack(int c) {
        assert peeked == 0 : String.format("Attempt to putBack '%c' at line %d and column %d with char '%c' already stored: ", c, line, column, peeked);
        peeked = c;
    }
}
