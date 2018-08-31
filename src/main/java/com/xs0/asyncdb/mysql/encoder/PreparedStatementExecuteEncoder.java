package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.mysql.binary.BinaryRowEncoder;
import com.xs0.asyncdb.mysql.binary.encoder.BinaryEncoder;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.client.PreparedStatementExecuteMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;
import java.util.Set;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newPacketBuffer;
import static com.xs0.asyncdb.mysql.column.ColumnType.FIELD_TYPE_NULL;

public class PreparedStatementExecuteEncoder implements MessageEncoder {
    private final BinaryRowEncoder rowEncoder;

    public PreparedStatementExecuteEncoder(BinaryRowEncoder rowEncoder) {
        this.rowEncoder = rowEncoder;
    }

    @Override
    public ByteBuf encode(ClientMessage message) {
        PreparedStatementExecuteMessage m = (PreparedStatementExecuteMessage) message;

        ByteBuf buffer = newPacketBuffer();
        buffer.writeByte(m.kind());
        buffer.writeBytes(m.statementId);
        buffer.writeByte(0x00); // no cursor
        buffer.writeInt(1);

        if (m.parameters == null || m.parameters.isEmpty()) {
            return buffer;
        } else {
            return Unpooled.wrappedBuffer(buffer, encodeValues(m.values, m.valuesToInclude));
        }
    }

    ByteBuf encodeValues(List<Object> values, Set<Integer> valuesToInclude) {
        int nullBitsCount = (values.size() + 7) / 8;
        byte[] nullBits = new byte[nullBitsCount];
        ByteBuf bitMapBuffer = newMysqlBuffer(1 + nullBitsCount);
        ByteBuf parameterTypesBuffer = newMysqlBuffer(values.size() * 2);
        ByteBuf parameterValuesBuffer = newMysqlBuffer();

        int index = 0;
        for (Object value : values) {
            // TODO - add support for Optional and the like? (downside = instanceof checks all the time, even if unused)

            if (value == null) {
                nullBits[index / 8] |= 1 << (index & 7);
                parameterTypesBuffer.writeShort(FIELD_TYPE_NULL);
            } else {
                encodeValue(parameterTypesBuffer, parameterValuesBuffer, value, valuesToInclude.contains(index));
            }

            index++;
        }

        bitMapBuffer.writeBytes(nullBits);
        bitMapBuffer.writeByte(values.isEmpty() ? 0 : 1);

        return Unpooled.wrappedBuffer(bitMapBuffer, parameterTypesBuffer, parameterValuesBuffer);
    }

    private void encodeValue(ByteBuf parameterTypesBuffer, ByteBuf parameterValuesBuffer, Object value, boolean includeValue) {
        BinaryEncoder encoder = rowEncoder.encoderFor(value);
        parameterTypesBuffer.writeShort(encoder.encodesTo());
        if (includeValue)
            encoder.encode(value, parameterValuesBuffer);
    }
}