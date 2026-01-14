package racs.clients.pack;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;
import racs.clients.exception.RacsException;
import racs.clients.types.Complex64;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Unpacker {

    public Object unpack(byte[] bytes) throws IOException, RacsException {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        int arraySize = unpacker.unpackArrayHeader();

        ValueType valueType = unpacker.getNextFormat().getValueType();
        if (valueType != ValueType.STRING)
            throw new RacsException("Invalid type");

        Object result = null;
        String type = unpacker.unpackString();

        switch (type) {
            case "string"       -> result = unpackString(unpacker);
            case "bool"         -> result = unpackBoolean(unpacker);
            case "int"          -> result = unpackLong(unpacker);
            case "float"        -> result = unpackDouble(unpacker);
            case "list"         -> result = unpackList(unpacker, arraySize - 1);
            case "time"         -> result = unpackTime(unpacker);
            case "u8v" , "s8v"  -> result = unpackByteArray(unpacker);
            case "u16v", "s16v" -> result = unpackShortArray(unpacker);
            case "u32v", "s32v" -> result = unpackIntArray(unpacker);
            case "f32v"         -> result = unpackFloatArray(unpacker);
            case "c64v"         -> result = unpackComplex64Array(unpacker);
            case "null"         -> result = unpackNull(unpacker);
            case "error"        -> unpackError(unpacker);
            default             -> throw new RacsException("Error unpacking response");
        }

        return result;
    }

    private String unpackString(MessageUnpacker unpacker) throws IOException {
        return unpacker.unpackString();
    }

    private Boolean unpackBoolean(MessageUnpacker unpacker) throws IOException {
        return unpacker.unpackBoolean();
    }

    private Long unpackLong(MessageUnpacker unpacker) throws IOException {
        return unpacker.unpackLong();
    }

    private Double unpackDouble(MessageUnpacker unpacker) throws IOException {
        return unpacker.unpackDouble();
    }

    private Object unpackNull(MessageUnpacker unpacker) throws IOException {
        return null;
    }

    private byte[] unpackByteArray(MessageUnpacker unpacker) throws IOException {
        int length = unpacker.unpackBinaryHeader();
        return unpacker.readPayload(length);
    }

    private short[] unpackShortArray(MessageUnpacker unpacker) throws IOException {
        byte[] bytes = unpackByteArray(unpacker);
        if (bytes == null) return new short[0];

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        short[] data = new short[bytes.length / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.getShort();
        }

        return data;
    }

    private int[] unpackIntArray(MessageUnpacker unpacker) throws IOException {
        byte[] bytes = unpackByteArray(unpacker);
        if (bytes == null) return new int[0];

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        int[] data = new int[bytes.length / 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.getInt();
        }

        return data;
    }

    private float[] unpackFloatArray(MessageUnpacker unpacker) throws IOException {
        byte[] bytes = unpackByteArray(unpacker);
        if (bytes == null) return new float[0];

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        float[] data = new float[bytes.length / 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.getFloat();
        }

        return data;
    }

    private Complex64[] unpackComplex64Array(MessageUnpacker unpacker) throws IOException {
        byte[] bytes = unpackByteArray(unpacker);
        if (bytes == null) return new Complex64[0];

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        Complex64[] data = new Complex64[bytes.length / 8];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Complex64(buf.getFloat(), buf.getFloat());
        }

        return data;
    }

    private void unpackError(MessageUnpacker unpacker) throws IOException, RacsException {
        throw new RacsException(unpacker.unpackString());
    }

    private OffsetDateTime unpackTime(MessageUnpacker unpacker) throws IOException {
        return Instant.ofEpochMilli(unpacker.unpackLong())
                .atOffset(ZoneOffset.UTC);
    }

    private Object unpackList(MessageUnpacker unpacker, int size) throws IOException, RacsException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ValueType valueType = unpacker.getNextFormat().getValueType();
            switch (valueType) {
                case STRING -> list.add(unpackString(unpacker));
                case FLOAT -> list.add(unpackDouble(unpacker));
                case INTEGER -> list.add(unpackLong(unpacker));
                case NIL -> list.add(unpackNull(unpacker));
                default -> throw new RacsException(String.format("Cannot unpack list element %s", valueType.name()));
            }
        }

        return list;
    }
}

