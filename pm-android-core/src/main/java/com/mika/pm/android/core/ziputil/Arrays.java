package com.mika.pm.android.core.ziputil;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:02
 * @Description:
 */
public class Arrays {
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
//            throw new ArrayIndexOutOfBoundsException(arrayLength, offset,
//                count);
            throw new ArrayIndexOutOfBoundsException(offset);
        }
    }
}
