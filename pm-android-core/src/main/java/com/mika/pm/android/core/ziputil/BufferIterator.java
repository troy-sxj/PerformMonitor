package com.mika.pm.android.core.ziputil;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:02
 * @Description:
 */
public abstract class BufferIterator {
    /**
     * Seeks to the absolute position {@code offset}, measured in bytes from the start.
     */
    public abstract void seek(int offset);
    /**
     * Skips forwards or backwards {@code byteCount} bytes from the current position.
     */
    public abstract void skip(int byteCount);

    public abstract int readInt();

    public abstract short readShort();
}
