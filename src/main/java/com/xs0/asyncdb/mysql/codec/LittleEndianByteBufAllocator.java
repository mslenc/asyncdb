package com.xs0.asyncdb.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.nio.ByteOrder;

class LittleEndianByteBufAllocator implements ByteBufAllocator {
    private static final LittleEndianByteBufAllocator instance = new LittleEndianByteBufAllocator();

    public static LittleEndianByteBufAllocator instance() {
        return instance;
    }

    private final UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator(false);

    private ByteBuf littleEndian(ByteBuf byteBuf) {
        return byteBuf.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public boolean isDirectBufferPooled() {
        return false;
    }

    @Override
    public int calculateNewCapacity(int minNewCapacity, int maxCapacity) {
        return allocator.calculateNewCapacity(minNewCapacity, maxCapacity);
    }

    @Override
    public ByteBuf buffer() {
        return littleEndian(allocator.buffer());
    }

    @Override
    public ByteBuf buffer(int initialCapacity) {
        return littleEndian(allocator.buffer(initialCapacity));
    }

    @Override
    public ByteBuf buffer(int initialCapacity, int maxCapacity) {
        return littleEndian(allocator.buffer(initialCapacity, maxCapacity));
    }

    @Override
    public ByteBuf ioBuffer() {
        return littleEndian(allocator.ioBuffer());
    }

    @Override
    public ByteBuf ioBuffer(int initialCapacity) {
        return littleEndian(allocator.ioBuffer(initialCapacity));
    }

    @Override
    public ByteBuf ioBuffer(int initialCapacity, int maxCapacity) {
        return littleEndian(allocator.ioBuffer(initialCapacity, maxCapacity));
    }

    @Override
    public ByteBuf heapBuffer() {
        return littleEndian(allocator.heapBuffer());
    }

    @Override
    public ByteBuf heapBuffer(int initialCapacity) {
        return littleEndian(allocator.heapBuffer(initialCapacity));
    }

    @Override
    public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
        return littleEndian(allocator.heapBuffer(initialCapacity, maxCapacity));
    }

    @Override
    public ByteBuf directBuffer() {
        return littleEndian(allocator.directBuffer());
    }

    @Override
    public ByteBuf directBuffer(int initialCapacity) {
        return littleEndian(allocator.directBuffer(initialCapacity));
    }

    @Override
    public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
        return littleEndian(allocator.directBuffer(initialCapacity, maxCapacity));
    }

    @Override
    public CompositeByteBuf compositeBuffer() {
        return allocator.compositeBuffer();
    }

    @Override
    public CompositeByteBuf compositeBuffer(int maxNumComponents) {
        return allocator.compositeBuffer(maxNumComponents);
    }

    @Override
    public CompositeByteBuf compositeHeapBuffer() {
        return allocator.compositeHeapBuffer();
    }

    @Override
    public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
        return allocator.compositeHeapBuffer(maxNumComponents);
    }

    @Override
    public CompositeByteBuf compositeDirectBuffer() {
        return allocator.compositeDirectBuffer();
    }

    @Override
    public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
        return allocator.compositeDirectBuffer(maxNumComponents);
    }
}
