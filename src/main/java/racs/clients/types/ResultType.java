package racs.clients.types;

public enum ResultType {
    STRING("string"),
    BOOLEAN("bool"),
    LONG("int"),
    DOUBLE("float"),
    TIME("time"),
    U8V("u8v"),
    I8V("s8v"),
    U16V("u16v"),
    I16V("s16v"),
    U32V("u32v"),
    I32V("s32v"),
    F32V("f32v"),
    C64V("c64v"),
    LIST("list"),
    NULL("null"),
    ERROR("error");

    private final String value;

    ResultType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
