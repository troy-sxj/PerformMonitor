package com.mika.pm.android.memory.hproflib.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * @Author: mika
 * @Time: 2019-11-05 17:14
 * @Description:
 */
public class ID {

    private final byte[] mIdBytes;

    public static ID createNullID(int size){
        return new ID(new byte[size]);
    }

    public ID(byte[] idBytes){
        final int len = idBytes.length;
        mIdBytes = new byte[len];
        System.arraycopy(idBytes, 0, mIdBytes, 0, len);
    }

    public byte[] getBytes() {
        return mIdBytes;
    }

    public int getSize(){
        return mIdBytes.length;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof ID)){
            return false;
        }
        return Arrays.equals(mIdBytes, ((ID) obj).mIdBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mIdBytes);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        for(byte b: mIdBytes){
            final int eb = b & 0xFF;
            stringBuilder.append(Integer.toHexString(eb));
        }
        return stringBuilder.toString();
    }
}
