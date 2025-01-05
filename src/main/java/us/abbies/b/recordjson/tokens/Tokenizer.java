package us.abbies.b.recordjson.tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator<Token> {
    private final Reader input;
    private int line;
    private int character;
    private String errorMessage;
    private Token onDeck;

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
        }

        int c = discardWhitespace();
        onDeck = switch (c) {
            case '{' -> new Token(TokenType.OBJ_START, null, line, character);
            case '}' -> new Token(TokenType.OBJ_END, null, line, character);
            case ':' -> new Token(TokenType.OBJ_NAME_SEP, null, line, character);
            case ',' -> new Token(TokenType.OBJ_VAL_SEP, null, line, character);
            case '[' -> new Token(TokenType.ARRAY_START, null, line, character);
            case ']' -> new Token(TokenType.ARRAY_END, null, line, character);
            case '"' -> {
                // TODO: read string
                yield new Token(TokenType.ERROR, "Strings unimplemented", line, character);
            }
            case 'f' -> {
                // TODO: read false
                yield new Token(TokenType.ERROR, "Booleans unimplemented", line, character);
            }
            case 'n' -> {
                // TODO: read null
                yield new Token(TokenType.ERROR, "Nulls unimplemented", line, character);
            }
            case 't' -> {
                // TODO: read true
                yield new Token(TokenType.ERROR, "Booleans unimplemented", line, character);
            }
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                // TODO: read number
                yield new Token(TokenType.ERROR, "Numbers unimplemented", line, character);
            }
            case -1 -> null;
            case -2 -> new Token(TokenType.ERROR, errorMessage, line, character);
            default -> new Token(TokenType.ERROR, "Unrecognized character: " + Character.toString(c), line, character);
        };
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
                character = 0;
            } else {
                character++;
            }
        } while (result == 0x20 || result == 0x09 || result == 0x0A || result == 0x0D);
        return result;
    }
}
