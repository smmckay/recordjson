package us.abbies.b.recordjson.tokens;

public enum TokenType {
    OBJ_START,
    OBJ_END,
    OBJ_NAME_SEP,
    OBJ_VAL_SEP,

    ARRAY_START,
    ARRAY_END,

    LIT_STR,
    LIT_NULL,
    LIT_BOOL,
    LIT_LONG,
    LIT_DOUBLE,

    ERROR
}
