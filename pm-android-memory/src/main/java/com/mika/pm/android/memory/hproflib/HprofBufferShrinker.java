package com.mika.pm.android.memory.hproflib;

import com.mika.pm.android.core.util.DigestUtil;
import com.mika.pm.android.core.util.PMUtil;
import com.mika.pm.android.memory.hproflib.model.Field;
import com.mika.pm.android.memory.hproflib.model.ID;
import com.mika.pm.android.memory.hproflib.model.Type;
import com.mika.pm.android.memory.hproflib.utils.IOUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @Author: mika
 * @Time: 2019-11-05 17:12
 * @Description:
 */
public class HprofBufferShrinker {

    public static final String TAG = "HprofBufferShrinker";

    private static final String PROPERTY_NAME = "extra.info";

    private final Set<ID> mBmpBufferIds = new HashSet<>();
    private final Map<ID, byte[]> mBufferIdToElementDataMap = new HashMap<>();
    private final Map<ID, ID> mBmpBufferIdToDeduplicatedIdMap = new HashMap<>();
    private final Set<ID> mStringValueIds = new HashSet<>();

    private ID mBitmapClassNameStringId = null;
    private ID mBmpClassId = null;
    private ID mMBufferFieldNameStringId = null;
    private ID mMRecycledFieldNameStringId = null;

    private ID mStringClassNameStringId = null;
    private ID mStringClassId = null;
    private ID mValueFieldNameStringId = null;

    private int mIdSize = 0;
    private ID mNullBufferId = null;
    private Field[] mBmpClassInstanceFields = null;
    private Field[] mStringClassInstanceFields = null;


    public static boolean addExtraInfo(File shrinkResultFile, Properties properties) {
        if (shrinkResultFile == null || !shrinkResultFile.exists()) {
            return false;
        }
        if (properties.isEmpty()) {
            return true;
        }
        long start = System.currentTimeMillis();
        OutputStream propertiesOutputStream = null;
        File propertiesFile = new File(shrinkResultFile.getParentFile(), PROPERTY_NAME);
        File tempFile = new File(shrinkResultFile.getAbsolutePath() + "_temp");

        try {
            propertiesOutputStream = new BufferedOutputStream(new FileOutputStream(propertiesFile, false));
            properties.store(propertiesOutputStream, null);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            PMUtil.closeQuietly(propertiesOutputStream);
        }
        return false;
    }

    public void shrink(File hprofIn, File hprofOut) throws IOException {
        FileInputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(hprofIn);
            os = new BufferedOutputStream(new FileOutputStream(hprofOut));

            final HprofReader reader = new HprofReader(new BufferedInputStream(is));
            reader.accept(new HprofInfoCollectVisitor());

            //转换后的hprof写本地
            is.getChannel().position(0);
            reader.accept(new HprofTransformWriter());

            // Reset.
            is.getChannel().position(0);
            reader.accept(new HprofKeptBufferCollectVisitor());
            // Reset.
            is.getChannel().position(0);
            reader.accept(new HprofBufferShrinkVisitor(new HprofWriter(os)));
        }finally {
            if(os != null){
                PMUtil.closeQuietly(os);
            }
            if(is != null){
                PMUtil.closeQuietly(is);
            }
        }
    }

    private class HprofInfoCollectVisitor extends HprofVisitor {

        HprofInfoCollectVisitor() {
            super(null);
        }

        @Override
        public void visitHeader(String text, int idSize, long timestamp) {
            mIdSize = idSize;
            mNullBufferId = ID.createNullID(idSize);
        }

        @Override
        public void visitStringRecord(ID id, String text, int timestamp, long length) {
            if (mBitmapClassNameStringId == null && "android.graphics.Bitmap".equals(text)) {
                mBitmapClassNameStringId = id;
            } else if (mMBufferFieldNameStringId == null && "mBuffer".equals(text)) {
                mMBufferFieldNameStringId = id;
            } else if (mMRecycledFieldNameStringId == null && "mRecycled".equals(text)) {
                mMRecycledFieldNameStringId = id;
            } else if (mStringClassNameStringId == null && "java.lang.String".equals(text)) {
                mStringClassNameStringId = id;
            } else if (mValueFieldNameStringId == null && "value".equals(text)) {
                mValueFieldNameStringId = id;
            }
        }

        @Override
        public void visitLoadClassRecord(int serialNumber, ID classObjectId, int stackTraceSerial, ID classNameStringId, int timestamp, long length) {
            if (mBmpClassId == null && mBitmapClassNameStringId != null && mBitmapClassNameStringId.equals(classNameStringId)) {
                mBmpClassId = classObjectId;
            } else if (mStringClassId == null && mStringClassNameStringId != null && mStringClassNameStringId.equals(classNameStringId)) {
                mStringClassId = classObjectId;
            }
        }

        @Override
        public HprofHeapDumpVisitor visitHeapDumpRecord(int tag, int timestamp, long length) {
            return new HprofHeapDumpVisitor(null) {
                @Override
                public void visitHeapDumpClass(ID id, int stackSerialNumber, ID superClassId, ID classLoaderId, int instanceSize, Field[] staticFields, Field[] instanceFields) {
                    if (mBmpClassInstanceFields == null && mBmpClassId != null && mBmpClassId.equals(id)) {
                        mBmpClassInstanceFields = instanceFields;
                    } else if (mStringClassInstanceFields == null && mStringClassId != null && mStringClassId.equals(id)) {
                        mStringClassInstanceFields = instanceFields;
                    }
                }
            };
        }
    }

    private class HprofKeptBufferCollectVisitor extends HprofVisitor {

        HprofKeptBufferCollectVisitor() {
            super(null);
        }

        @Override
        public HprofHeapDumpVisitor visitHeapDumpRecord(int tag, int timestamp, long length) {
            return new HprofHeapDumpVisitor(null) {

                @Override
                public void visitHeapDumpInstance(ID id, int stackId, ID typeId, byte[] instanceData) {
                    try {
                        if (mBmpClassId != null && mBmpClassId.equals(typeId)) {
                            ID bufferId = null;
                            Boolean isRecycled = null;
                            final ByteArrayInputStream bais = new ByteArrayInputStream(instanceData);
                            for (Field field : mBmpClassInstanceFields) {
                                final ID fieldNameStringId = field.nameId;
                                final Type fieldType = Type.getType(field.typeId);
                                if (fieldType == null) {
                                    throw new IllegalStateException("visit bmp instance failed, lost type def of typeId: " + field.typeId);
                                }
                                if (mMBufferFieldNameStringId.equals(fieldNameStringId)) {
                                    bufferId = (ID) IOUtil.readValue(bais, fieldType, mIdSize);
                                } else if (mMRecycledFieldNameStringId.equals(fieldNameStringId)) {
                                    isRecycled = (Boolean) IOUtil.readValue(bais, fieldType, mIdSize);
                                } else if (bufferId == null || isRecycled == null) {
                                    IOUtil.skipValue(bais, fieldType, mIdSize);
                                } else {
                                    break;
                                }
                            }
                            bais.close();
                            final boolean reguardAsNotRecycledBmp = (isRecycled == null || !isRecycled);
                            if (bufferId != null && reguardAsNotRecycledBmp && !bufferId.equals(mNullBufferId)) {
                                mBmpBufferIds.add(bufferId);
                            }
                        } else if (mStringClassId != null && mStringClassId.equals(typeId)) {
                            ID strValueId = null;
                            final ByteArrayInputStream bais = new ByteArrayInputStream(instanceData);
                            for (Field field : mStringClassInstanceFields) {
                                final ID fieldNameStringId = field.nameId;
                                final Type fieldType = Type.getType(field.typeId);
                                if (fieldType == null) {
                                    throw new IllegalStateException("visit string instance failed, lost type def of typeId: " + field.typeId);
                                }
                                if (mValueFieldNameStringId.equals(fieldNameStringId)) {
                                    strValueId = (ID) IOUtil.readValue(bais, fieldType, mIdSize);
                                } else if (strValueId == null) {
                                    IOUtil.skipValue(bais, fieldType, mIdSize);
                                } else {
                                    break;
                                }
                            }
                            bais.close();
                            if (strValueId != null && !strValueId.equals(mNullBufferId)) {
                                mStringValueIds.add(strValueId);
                            }
                        }
                    } catch (Throwable thr) {
                        throw new RuntimeException(thr);
                    }
                }

                @Override
                public void visitHeapDumpPrimitiveArray(int tag, ID id, int stackId, int numElements, int typeId, byte[] elements) {
                    mBufferIdToElementDataMap.put(id, elements);
                }
            };
        }

        @Override
        public void visitEnd() {
            final Set<Map.Entry<ID, byte[]>> idDataSet = mBufferIdToElementDataMap.entrySet();
            final Map<String, ID> duplicateBufferFilterMap = new HashMap<>();
            for (Map.Entry<ID, byte[]> idDataPair : idDataSet) {
                final ID bufferId = idDataPair.getKey();
                final byte[] elementData = idDataPair.getValue();
                if (!mBmpBufferIds.contains(bufferId)) {
                    // Discard non-bitmap buffer.
                    continue;
                }
                final String buffMd5 = DigestUtil.getMD5String(elementData);
                final ID mergedBufferId = duplicateBufferFilterMap.get(buffMd5);
                if (mergedBufferId == null) {
                    duplicateBufferFilterMap.put(buffMd5, bufferId);
                } else {
                    mBmpBufferIdToDeduplicatedIdMap.put(mergedBufferId, mergedBufferId);
                    mBmpBufferIdToDeduplicatedIdMap.put(bufferId, mergedBufferId);
                }
            }
            // Save memory cost.
            mBufferIdToElementDataMap.clear();
        }
    }

    private class HprofBufferShrinkVisitor extends HprofVisitor {

        HprofBufferShrinkVisitor(HprofWriter hprofWriter) {
            super(hprofWriter);
        }

        @Override
        public HprofHeapDumpVisitor visitHeapDumpRecord(int tag, int timestamp, long length) {
            return new HprofHeapDumpVisitor(super.visitHeapDumpRecord(tag, timestamp, length)) {
                @Override
                public void visitHeapDumpInstance(ID id, int stackId, ID typeId, byte[] instanceData) {
                    try {
                        if (typeId.equals(mBmpClassId)) {
                            ID bufferId = null;
                            int bufferIdPos = 0;
                            final ByteArrayInputStream bais = new ByteArrayInputStream(instanceData);
                            for (Field field : mBmpClassInstanceFields) {
                                final ID fieldNameStringId = field.nameId;
                                final Type fieldType = Type.getType(field.typeId);
                                if (fieldType == null) {
                                    throw new IllegalStateException("visit instance failed, lost type def of typeId: " + field.typeId);
                                }
                                if (mMBufferFieldNameStringId.equals(fieldNameStringId)) {
                                    bufferId = (ID) IOUtil.readValue(bais, fieldType, mIdSize);
                                    break;
                                } else {
                                    bufferIdPos += IOUtil.skipValue(bais, fieldType, mIdSize);
                                }
                            }
                            if (bufferId != null) {
                                final ID deduplicatedId = mBmpBufferIdToDeduplicatedIdMap.get(bufferId);
                                if (deduplicatedId != null && !bufferId.equals(deduplicatedId) && !bufferId.equals(mNullBufferId)) {
                                    modifyIdInBuffer(instanceData, bufferIdPos, deduplicatedId);
                                }
                            }
                        }
                    } catch (Throwable thr) {
                        throw new RuntimeException(thr);
                    }
                    super.visitHeapDumpInstance(id, stackId, typeId, instanceData);
                }

                private void modifyIdInBuffer(byte[] buf, int off, ID newId) {
                    final ByteBuffer bBuf = ByteBuffer.wrap(buf);
                    bBuf.position(off);
                    bBuf.put(newId.getBytes());
                }

                @Override
                public void visitHeapDumpPrimitiveArray(int tag, ID id, int stackId, int numElements, int typeId, byte[] elements) {
                    final ID deduplicatedID = mBmpBufferIdToDeduplicatedIdMap.get(id);
                    // Discard non-bitmap or duplicated bitmap buffer but keep reference key.
                    if (deduplicatedID == null || !id.equals(deduplicatedID)) {
                        if (!mStringValueIds.contains(id)) {
                            return;
                        }
                    }
                    super.visitHeapDumpPrimitiveArray(tag, id, stackId, numElements, typeId, elements);
                }
            };
        }
    }

}
