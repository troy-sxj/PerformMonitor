package com.mika.pm.android.memory.hproflib;

import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.hproflib.model.Field;
import com.mika.pm.android.memory.hproflib.model.ID;
import com.mika.pm.android.memory.hproflib.model.Type;
import com.mika.pm.android.memory.hproflib.utils.IOUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:19
 * @Description:
 */
public class HprofReader {

    private static final String TAG = "HprofReader";
    private final InputStream mStreamIn;
    private int mIdSize = 0;

    public HprofReader(InputStream in) {
        mStreamIn = in;
    }

    public void accept(HprofVisitor hv) throws IOException {
        acceptHeader(hv);
        acceptRecord(hv);
        hv.visitEnd();
    }

    private void acceptHeader(HprofVisitor hv) throws IOException {
        //读取版本信息
        final String text = IOUtil.readNullTerminatedString(mStreamIn);
        final int idSize = IOUtil.readBEInt(mStreamIn);
        if (idSize <= 0 || idSize >= (Integer.MAX_VALUE >> 1)) {
            throw new IOException("bad idSize: " + idSize);
        }
        final long timestamp = IOUtil.readBELong(mStreamIn);
        mIdSize = idSize;
        hv.visitHeader(text, idSize, timestamp);
    }

    private void acceptRecord(HprofVisitor hv) throws IOException {
        try {
            while (true) {
                final int tag = mStreamIn.read();
                final int timestamp = IOUtil.readBEInt(mStreamIn);
                final long length = IOUtil.readBEInt(mStreamIn) & 0x00000000FFFFFFFFL;
                switch (tag) {
                    case HprofConstants.RECORD_TAG_STRING:  //string
                        acceptStringRecord(timestamp, length, hv);
                        break;
                    case HprofConstants.RECORD_TAG_LOAD_CLASS:  //已加载的class
                        acceptLoadClassRecord(timestamp, length, hv);
                        break;
                    case HprofConstants.RECORD_TAG_STACK_FRAME: //包含所有线程的栈帧信息
                        acceptStackFrameRecord(timestamp, length, hv);
                        break;
                    case HprofConstants.RECORD_TAG_STACK_TRACE: //包含所有线程的虚拟机栈情况
                        acceptStackTraceRecord(timestamp, length, hv);
                        break;
                    case HprofConstants.RECORD_TAG_HEAP_DUMP:
                    case HprofConstants.RECORD_TAG_HEAP_DUMP_SEGMENT:
                        acceptHeapDumpRecord(tag, timestamp, length, hv);
                        break;
                    case HprofConstants.RECORD_TAG_ALLOC_SITES:
                    case HprofConstants.RECORD_TAG_HEAP_SUMMARY:
                        acceptHeapSummary(timestamp, length, hv);
                    case HprofConstants.RECORD_TAG_START_THREAD:
                        PMLog.e(TAG, "RECORD_TAG_START_THREAD");
                        acceptThreadStart(timestamp, length, hv);
                    case HprofConstants.RECORD_TAG_END_THREAD:
                        acceptThreadEnd(timestamp, length, hv);
                    case HprofConstants.RECORD_TAG_HEAP_DUMP_END:
                    case HprofConstants.RECORD_TAG_CPU_SAMPLES:
                    case HprofConstants.RECORD_TAG_CONTROL_SETTINGS:
                    case HprofConstants.RECORD_TAG_UNLOAD_CLASS:
                    case HprofConstants.RECORD_TAG_UNKNOWN:
                    default:
                        acceptUnconcernedRecord(tag, timestamp, length, hv);
                        break;
                }
            }
        } catch (EOFException ignored) {
            // Ignored.
        }
    }

    private void acceptThreadStart(int timestamp, long length, HprofVisitor hv) throws IOException{
        final int serialNumber = IOUtil.readBEInt(mStreamIn);
        final ID threadObjId = IOUtil.readID(mStreamIn, mIdSize);
        final int stacktraceSerial = IOUtil.readBEInt(mStreamIn);
        final ID threadNameStrId = IOUtil.readID(mStreamIn, mIdSize);
        final ID threadGroupNameId = IOUtil.readID(mStreamIn, mIdSize);
        final ID threadParentGroupNameId = IOUtil.readID(mStreamIn, mIdSize);
        hv.visitThreadStartRecord(serialNumber, threadObjId, stacktraceSerial, threadNameStrId, threadGroupNameId, threadParentGroupNameId);
    }

    private void acceptThreadEnd(int timestamp, long length, HprofVisitor hv) throws IOException{
        final int serialNumber = IOUtil.readBEInt(mStreamIn);
        hv.visitThreadEnd(serialNumber);
    }

    private void acceptHeapSummary(int timestamp, long length, HprofVisitor hv) throws IOException {
        final int totalLiveBytes = IOUtil.readBEInt(mStreamIn);
        final int totalLiveInstances = IOUtil.readBEInt(mStreamIn);
        final long totalBytesAllocated = IOUtil.readBELong(mStreamIn);
        final long totalInstancesAllocated = IOUtil.readBELong(mStreamIn);
        PMLog.e(TAG, "acceptHeapSummary === live bytes: %d, live instances: %d, bytes allocated: %l, instances allocated: %l",
                totalLiveBytes, totalLiveInstances, totalBytesAllocated, totalInstancesAllocated);
    }

    private void acceptStringRecord(int timestamp, long length, HprofVisitor hv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final String text = IOUtil.readString(mStreamIn, length - mIdSize);
        hv.visitStringRecord(id, text, timestamp, length);
    }

    private void acceptLoadClassRecord(int timestamp, long length, HprofVisitor hv) throws IOException {
        final int serialNumber = IOUtil.readBEInt(mStreamIn);
        final ID classObjectId = IOUtil.readID(mStreamIn, mIdSize);
        final int stackTraceSerial = IOUtil.readBEInt(mStreamIn);
        final ID classNameStringId = IOUtil.readID(mStreamIn, mIdSize);
        hv.visitLoadClassRecord(serialNumber, classObjectId, stackTraceSerial, classNameStringId, timestamp, length);
    }

    private void acceptStackFrameRecord(int timestamp, long length, HprofVisitor hv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final ID methodNameId = IOUtil.readID(mStreamIn, mIdSize);
        final ID methodSignatureId = IOUtil.readID(mStreamIn, mIdSize);
        final ID sourceFileId = IOUtil.readID(mStreamIn, mIdSize);
        final int serial = IOUtil.readBEInt(mStreamIn);
        final int lineNumber = IOUtil.readBEInt(mStreamIn);
        hv.visitStackFrameRecord(id, methodNameId, methodSignatureId, sourceFileId, serial, lineNumber, timestamp, length);
    }

    private void acceptStackTraceRecord(int timestamp, long length, HprofVisitor hv) throws IOException {
        final int serialNumber = IOUtil.readBEInt(mStreamIn);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        final int numFrames = IOUtil.readBEInt(mStreamIn);
        final ID[] frameIds = new ID[numFrames];
        for (int i = 0; i < numFrames; ++i) {
            frameIds[i] = IOUtil.readID(mStreamIn, mIdSize);
        }
        hv.visitStackTraceRecord(serialNumber, threadSerialNumber, frameIds, timestamp, length);
    }

    private void acceptHeapDumpRecord(int tag, int timestamp, long length, HprofVisitor hv) throws IOException {
        final HprofHeapDumpVisitor hdv = hv.visitHeapDumpRecord(tag, timestamp, length);
        if (hdv == null) {
            IOUtil.skip(mStreamIn, length);
            return;
        }
        while (length > 0) {
            final int heapDumpTag = mStreamIn.read();
            --length;
            switch (heapDumpTag) {
                case HprofConstants.HEAPDUMP_ROOT_UNKNOWN:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_UNKNOWN", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_JNI_GLOBAL:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_JNI_GLOBAL", IOUtil.readID(mStreamIn, mIdSize));
                    IOUtil.skip(mStreamIn, mIdSize);   //  ignored
                    length -= (mIdSize << 1);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_JNI_LOCAL:
                    length -= acceptJniLocal(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_JAVA_FRAME:
                    length -= acceptJavaFrame(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_NATIVE_STACK:
                    length -= acceptNativeStack(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_STICKY_CLASS:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_STICKY_CLASS", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_THREAD_BLOCK:
                    length -= acceptThreadBlock(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_MONITOR_USED:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_MONITOR_USED", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_THREAD_OBJECT:
                    length -= acceptThreadObject(hdv);
                    break;


                case HprofConstants.HEAPDUMP_ROOT_CLASS_DUMP:
                    length -= acceptClassDump(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_INSTANCE_DUMP:
                    length -= acceptInstanceDump(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_OBJECT_ARRAY_DUMP:
                    length -= acceptObjectArrayDump(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_PRIMITIVE_ARRAY_DUMP:
                    length -= acceptPrimitiveArrayDump(heapDumpTag, hdv);
                    break;


                case HprofConstants.HEAPDUMP_ROOT_PRIMITIVE_ARRAY_NODATA_DUMP:
                    length -= acceptPrimitiveArrayDump(heapDumpTag, hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_HEAP_DUMP_INFO:
                    length -= acceptHeapDumpInfo(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_INTERNED_STRING:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_INTERNED_STRING", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_FINALIZING:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_FINALIZING", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_DEBUGGER:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_DEBUGGER", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_REFERENCE_CLEANUP:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_REFERENCE_CLEANUP", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_VM_INTERNAL:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_VM_INTERNAL", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                case HprofConstants.HEAPDUMP_ROOT_JNI_MONITOR:
                    length -= acceptJniMonitor(hdv);
                    break;
                case HprofConstants.HEAPDUMP_ROOT_UNREACHABLE:
                    hdv.visitHeapDumpBasicObj(heapDumpTag, "ROOT_UNREACHABLE", IOUtil.readID(mStreamIn, mIdSize));
                    length -= mIdSize;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "acceptHeapDumpRecord loop with unknown tag " + heapDumpTag
                                    + " with " + mStreamIn.available()
                                    + " bytes possibly remaining");
            }
        }
        hdv.visitEnd();
    }

    private void acceptUnconcernedRecord(int tag, int timestamp, long length, HprofVisitor hv) throws IOException {
        final byte[] data = new byte[(int) length];
        IOUtil.readFully(mStreamIn, data, 0, length);
        hv.visitUnconcernedRecord(tag, timestamp, length, data);
    }

    private int acceptHeapDumpInfo(HprofHeapDumpVisitor hdv) throws IOException {
        final int heapId = IOUtil.readBEInt(mStreamIn);
        final ID heapNameId = IOUtil.readID(mStreamIn, mIdSize);
        hdv.visitHeapDumpInfo(heapId, heapNameId);
        return 4 + mIdSize;
    }

    private int acceptJniLocal(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        final int stackFrameNumber = IOUtil.readBEInt(mStreamIn);
        hdv.visitHeapDumpJniLocal(id, threadSerialNumber, stackFrameNumber);
        return mIdSize + 4 + 4;
    }

    private int acceptJavaFrame(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        final int stackFrameNumber = IOUtil.readBEInt(mStreamIn);
//        PMLog.e(TAG, "ROOT JAVA FRAME === threadSerialNum: %d, stackFrameNum: %d", threadSerialNumber, stackFrameNumber);
        hdv.visitHeapDumpJavaFrame(id, threadSerialNumber, stackFrameNumber);
        return mIdSize + 4 + 4;
    }

    private int acceptNativeStack(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        hdv.visitHeapDumpNativeStack(id, threadSerialNumber);
        return mIdSize + 4;
    }

    private int acceptThreadBlock(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        hdv.visitHeapDumpThreadBlock(id, threadSerialNumber);
        return mIdSize + 4;
    }

    private int acceptThreadObject(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        final int stackFrameNumber = IOUtil.readBEInt(mStreamIn);
//        PMLog.e(TAG, "ROOT THREAD OBJECT === id: %s, thread serialNum: %d, stack frame num: %d", id.toString(), threadSerialNumber, stackFrameNumber);
        hdv.visitHeapDumpThreadObject(id, threadSerialNumber, stackFrameNumber);
        return mIdSize + 4 + 4;
    }

    private int acceptClassDump(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int stackSerialNumber = IOUtil.readBEInt(mStreamIn);
        final ID superClassId = IOUtil.readID(mStreamIn, mIdSize);
        final ID classLoaderId = IOUtil.readID(mStreamIn, mIdSize);
        //左移两位，跳过 signers object ID, protection domain object ID, reserved ID, reserved ID
        IOUtil.skip(mStreamIn, (mIdSize << 2));

        final int instanceSize = IOUtil.readBEInt(mStreamIn);

        int bytesRead = (7 * mIdSize) + 4 + 4;

        //  Skip over the constant pool
        int numEntries = IOUtil.readBEShort(mStreamIn);
        bytesRead += 2;
        for (int i = 0; i < numEntries; ++i) {
            IOUtil.skip(mStreamIn, 2);
            bytesRead += 2 + skipValue();
        }

        //  Static fields
        numEntries = IOUtil.readBEShort(mStreamIn);
        Field[] staticFields = new Field[numEntries];
        bytesRead += 2;
        for (int i = 0; i < numEntries; ++i) {
            final ID nameId = IOUtil.readID(mStreamIn, mIdSize);
            final int typeId = mStreamIn.read();
            final Type type = Type.getType(typeId);
            if (type == null) {
                throw new IllegalStateException("accept class failed, lost type def of typeId: " + typeId);
            }
            final Object staticValue = IOUtil.readValue(mStreamIn, type, mIdSize);
            staticFields[i] = new Field(typeId, nameId, staticValue);
            bytesRead += mIdSize + 1 + type.getSize(mIdSize);
        }

        //  Instance fields
        numEntries = IOUtil.readBEShort(mStreamIn);
        final Field[] instanceFields = new Field[numEntries];
        bytesRead += 2;
        for (int i = 0; i < numEntries; i++) {
            final ID nameId = IOUtil.readID(mStreamIn, mIdSize);
            final int typeId = mStreamIn.read();
            instanceFields[i] = new Field(typeId, nameId, null);
            bytesRead += mIdSize + 1;
        }

        hdv.visitHeapDumpClass(id, stackSerialNumber, superClassId, classLoaderId, instanceSize, staticFields, instanceFields);

        return bytesRead;
    }

    private int acceptInstanceDump(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int stackId = IOUtil.readBEInt(mStreamIn);
        final ID typeId = IOUtil.readID(mStreamIn, mIdSize);
        final int remaining = IOUtil.readBEInt(mStreamIn);
        final byte[] instanceData = new byte[remaining];
        IOUtil.readFully(mStreamIn, instanceData, 0, remaining);
        hdv.visitHeapDumpInstance(id, stackId, typeId, instanceData);
        return mIdSize + 4 + mIdSize + 4 + remaining;
    }

    private int acceptObjectArrayDump(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int stackId = IOUtil.readBEInt(mStreamIn);
        final int numElements = IOUtil.readBEInt(mStreamIn);
        final ID typeId = IOUtil.readID(mStreamIn, mIdSize);
        final int remaining = numElements * mIdSize;
        final byte[] elements = new byte[remaining];
        IOUtil.readFully(mStreamIn, elements, 0, remaining);
        hdv.visitHeapDumpObjectArray(id, stackId, numElements, typeId, elements);
        return mIdSize + 4 + 4 + mIdSize + remaining;
    }

    private int acceptPrimitiveArrayDump(int tag, HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int stackId = IOUtil.readBEInt(mStreamIn);
        final int numElements = IOUtil.readBEInt(mStreamIn);
        final int typeId = mStreamIn.read();
        final Type type = Type.getType(typeId);
        if (type == null) {
            throw new IllegalStateException("accept primitive array failed, lost type def of typeId: " + typeId);
        }
        final int remaining = numElements * type.getSize(mIdSize);
        final byte[] elements = new byte[remaining];
        IOUtil.readFully(mStreamIn, elements, 0, remaining);
        hdv.visitHeapDumpPrimitiveArray(tag, id, stackId, numElements, typeId, elements);
        return mIdSize + 4 + 4 + 1 + remaining;
    }

    private int acceptJniMonitor(HprofHeapDumpVisitor hdv) throws IOException {
        final ID id = IOUtil.readID(mStreamIn, mIdSize);
        final int threadSerialNumber = IOUtil.readBEInt(mStreamIn);
        final int stackDepth = IOUtil.readBEInt(mStreamIn);
        hdv.visitHeapDumpJniMonitor(id, threadSerialNumber, stackDepth);
        return mIdSize + 4 + 4;
    }

    private int skipValue() throws IOException {
        final int typeId = mStreamIn.read();
        final Type type = Type.getType(typeId);
        if (type == null) {
            throw new IllegalStateException("failure to skip type, cannot find type def of typeid: " + typeId);
        }
        final int size = type.getSize(mIdSize);
        IOUtil.skip(mStreamIn, size);
        return size + 1;
    }
}
