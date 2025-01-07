package us.abbies.b.recordjson.tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator<Token> {
    private final Reader input;
    private int line = 1;
    private int column;
    private Token onDeck;
    private boolean hitError;

    Tokenizer(Reader input) {
        this.input = input;
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
                case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> throw new Token.Exception("Numbers unimplemented", line, column);
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
        char c = read();
        return switch (c) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> throw new Token.Exception("Unicode escapes unimplemented", line, column);
            default -> throw new Token.Exception("Unrecognized escape sequence \\" + c, line, column);
        };
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
            try {
                result = input.read();
            } catch (IOException e) {
                throw new Token.Exception(e.getMessage(), e, line, column);
            }

            if (result == 0x0A) {
                line++;
                column = 0;
            } else {
                column++;
            }
        } while (result == 0x20 || result == 0x09 || result == 0x0A || result == 0x0D);
        return result;
    }

    private char read() {
        int result;
        try {
            result = input.read();
        } catch (IOException e) {
            throw new Token.Exception(e.getMessage(), e, line, column);
        }
        if (result == -1) {
            throw new Token.Exception("Unexpected end of input", line, column);
        }
        column++;
        return (char) result;
    }
}
