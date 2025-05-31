package us.abbies.b.recordjson.tokens;

import java.util.Random;

public class JsonGenerator {
    private final Random r;
    private final StringBuilder sb;

    public JsonGenerator(int seed) {
        r = new Random(seed);
        sb = new StringBuilder();
    }

    public String generate() {
        generateObject(500);
        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private void generateObject(int fieldCount) {
        sb.append('{');
        for (int i = 0; i < fieldCount; i++) {
            generateString(6, 24);
            sb.append(": ");
            generateValue();
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, '}');
    }

    private void generateValue() {
        int i = r.nextInt(256);
        if (i < 3) {
            generateObject(r.nextInt(20) + 1);
        } else if (i < 6) {
            generateArray(r.nextInt(10) + 1);
        } else if (i < 9) {
            generateNull();
        } else if (i < 20) {
            generateBool();
        } else if (i < 175) {
            generateString(1, 4096);
        } else {
            generateNumber();
        }
    }

    private void generateNumber() {
        if (r.nextBoolean()) {
            sb.append('-');
        }

        sb.append('0' + r.nextInt(9) + 1);
        for (int i = 0; i < r.nextInt(10); i++) {
            sb.append('0' + r.nextInt(10));
        }

        if (r.nextInt(8) == 0) {
            for (int i = 0; i < r.nextInt(10) + 1; i++) {
                sb.append('0' + r.nextInt(10));
            }
        }

        if (r.nextInt(8) == 0) {
            sb.append('e');
            for (int i = 0; i < r.nextInt(5) + 1; i++) {
                sb.append('0' + r.nextInt(10));
            }
        }
    }

    private void generateString(int minLength, int maxLength) {
        int length = r.nextInt(maxLength - minLength + 1) + minLength;

        sb.append('"');
        for (int i = 0; i < length; i++) {
            generateChar();
        }
        sb.append('"');
    }

    private void generateChar() {
        int i = r.nextInt(256);
        if (i < 3) {
            sb.append('\uD83D');
            sb.append('\uDCA9');
        } else if (i < 9) {
            sb.append('\\');
            sb.append('n');
        } else {
            sb.append('a');
        }
    }

    private void generateNull() {
        sb.append("null");
    }

    private void generateBool() {
        sb.append(r.nextBoolean() ? "true" : "false");
    }

    private void generateArray(int length) {
        sb.append('[');
        for (int i = 0; i < length; i++) {
            generateValue();
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }
}
