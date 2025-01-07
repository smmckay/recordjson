package us.abbies.b.recordjson.tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator<Token> {
    private final Reader input;
    private int line = 1;
    private int column;
    private String errorMessage;
    private Token onDeck;
    private boolean hitError;

    Tokenizer(Reader input) {
        this.input = input;
    }

    @Override
    public boolean hasNext() {
        if (hitError) {
            return false;
        } else if (onDeck != null) {
            return true;
        } else {
            tryReadToken();
        }
        return onDeck != null;
    }

    @Override
    public Token next() {
        if (hitError) {
            throw new NoSuchElementException();
        }
        if (onDeck == null) {
            tryReadToken();
        }
        if (onDeck == null) {
            throw new NoSuchElementException();
        }

        Token result = onDeck;
        if (result.type() == TokenType.ERROR) {
            hitError = true;
        }
        onDeck = null;
        return result;
    }

    private void tryReadToken() {
        if (onDeck != null) {
            throw new AssertionError("tryReadToken called with token on deck");
        }

        int c = discardWhitespace();
        onDeck = switch (c) {
            case '{' -> new Token(TokenType.OBJ_START, null, line, column);
            case '}' -> new Token(TokenType.OBJ_END, null, line, column);
            case ':' -> new Token(TokenType.OBJ_NAME_SEP, null, line, column);
            case ',' -> new Token(TokenType.OBJ_VAL_SEP, null, line, column);
            case '[' -> new Token(TokenType.ARRAY_START, null, line, column);
            case ']' -> new Token(TokenType.ARRAY_END, null, line, column);
            case '"' -> readString(column);
            case 'f' -> expect("alse", new Token(TokenType.LIT_BOOL, false, line, column));
            case 'n' -> expect("ull", new Token(TokenType.LIT_NULL, null, line, column));
            case 't' -> expect("rue", new Token(TokenType.LIT_BOOL, true, line, column));
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                // TODO: read number
                yield new Token(TokenType.ERROR, "Numbers unimplemented", line, column);
            }
            case -1 -> null;
            case -2 -> new Token(TokenType.ERROR, errorMessage, line, column);
            default -> new Token(TokenType.ERROR, "Unrecognized character: " + Character.toString(c), line, column);
        };
    }

    private Token readString(int startingColumn) {
        StringBuilder result = new StringBuilder();
        int c;
        do {
            try {
                c = input.read();
            } catch (IOException e) {
                return new Token(TokenType.ERROR, e.getMessage(), line, column);
            }
            if (c == -1) {
                return new Token(TokenType.ERROR, "Unexpected end of input", line, column);
            }

            column++;
            if (c == '\\') {
                return new Token(TokenType.ERROR, "Escapes unimplemented", line, column);
            } else if (c == '"') {
                return new Token(TokenType.LIT_STR, result.toString(), line, startingColumn);
            } else if (c <= 0x1F) {
                return new Token(TokenType.ERROR, "Control characters not allowed inside strings", line, column);
            } else {
                result.append((char) c);
            }
        } while (true);
    }

    private Token expect(String remaining, Token successToken) {
        for (char expected : remaining.toCharArray()) {
            int actual;
            try {
                actual = input.read();
            } catch (IOException e) {
                return new Token(TokenType.ERROR, e.getMessage(), line, column);
            }

            if (actual == -1) {
                return new Token(TokenType.ERROR, "Unexpected end of input", line, column);
            }

            column++;
            if (expected != actual) {
                return new Token(TokenType.ERROR, "Unexpected character: " + Character.toString(actual), line, column);
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
                errorMessage = e.getMessage();
                return -2;
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
}
