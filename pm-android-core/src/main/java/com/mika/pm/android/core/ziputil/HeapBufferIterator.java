package com.mika.pm.android.core.ziputil;

import java.nio.ByteOrder;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:03
 * @Description:
 */
public final class HeapBufferIterator extends BufferIterator {
    private final byte[] buffer;
    private final int offset;
    private final int byteCount;
    private final ByteOrder order;
    private int position;
    HeapBufferIterator(byte[] buffer, int offset, int byteCount, ByteOrder order) {
        this.buffer = buffer;
        this.offset = offset;
        this.byteCount = byteCount;
        this.order = order;
    }

    /**
     * Returns a new iterator over {@code buffer}, starting at {@code offset} and continuing for
     * {@code byteCount} bytes. Items larger than a byte are interpreted using the given byte order.
     */
    public static BufferIterator iterator(byte[] buffer, int offset, int byteCount, ByteOrder order) {
        return new HeapBufferIterator(buffer, offset, byteCount, order);
    }

    public void seek(int offset) {
        position = offset;
    }

    public void skip(int byteCount) {
        position += byteCount;
    }

    public void readByteArray(byte[] dst, int dstOffset, int byteCount) {
        System.arraycopy(buffer, offset + position, dst, dstOffset, byteCount);
        position += byteCount;
    }

    public byte readByte() {
        byte result = buffer[offset + position];
        ++position;
        return result;
    }

    public int readInt() {
        int result = Memory.peekInt(buffer, offset + position, order);
        position += SizeOf.INT;
        return result;
    }

    public short readShort() {
        short result = Memory.peekShort(buffer, offset + position, order);
        position += SizeOf.SHORT;
        return result;
    }
}
