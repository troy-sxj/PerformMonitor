package com.mika.pm.android.memory.hproflib.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: mika
 * @Time: 2019-11-05 17:25
 * @Description:
 */
public enum  Type {
    OBJECT(2, 0),
    BOOLEAN(4, 1),
    CHAR(5, 2),
    FLOAT(6, 4),
    DOUBLE(7, 8),
    BYTE(8, 1),
    SHORT(9, 2),
    INT(10, 4),
    LONG(11, 8);

    private static Map<Integer, Type> sTypeMap = new HashMap<>();

    private int mId;
    private int mSize;

    static {
        for(Type type: Type.values()){
            sTypeMap.put(type.mId, type);
        }
    }

    Type(int type, int size){
        mId = type;
        mSize = size;
    }

    public static Type getType(int id){
        return sTypeMap.get(id);
    }

    public int getSize(int idSize){
        return mSize !=0 ? mSize: idSize;
    }

    public int getTypeId(){
        return mId;
    }

    public static String getClassNameOfPrimitiveArray(Type type){
        switch (type){
            case BOOLEAN: return "boolean[]";
            case CHAR: return "char[]";
            case FLOAT: return "float[]";
            case DOUBLE: return "double[]";
            case BYTE: return "byte[]";
            case SHORT: return "short[]";
            case INT: return "int[]";
            case LONG: return "long[]";
            default: throw new IllegalArgumentException("OBJECT type is not a primitive type");
        }
    }

}
