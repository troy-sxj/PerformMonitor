package com.mika.pm.android.memory.hproflib;

import android.os.Environment;

import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.hproflib.model.Field;
import com.mika.pm.android.memory.hproflib.model.ID;
import com.mika.pm.android.memory.hproflib.model.Type;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @Author: mika
 * @Time: 2019-11-07 16:36
 * @Description:
 */
public class HprofTransformWriter extends HprofVisitor {

    private static final String TAG = "HprofTransformWriter";

    private BufferedOutputStream bufferedOutputStream;

    private boolean isFirstWriteHeader;

    /**
     * String Record HashMap
     * <p>
     * key: String ID
     * value: name(UTF8)
     * </p>
     */
    private HashMap<String, String> stringRecordMap = new HashMap<>();

    public HprofTransformWriter() {
        super(null);
        File dir = Environment.getExternalStoragePublicDirectory("AHprof");
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            PMLog.e(TAG, "create dir status:" + mkdirs);
        }
        File file = new File(dir, System.currentTimeMillis() + "_hprof_transformed.txt");
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitHeader(String text, int idSize, long timestamp) {
        super.visitHeader(text, idSize, timestamp);

        if (bufferedOutputStream != null) {
            try {
                if (isFirstWriteHeader) {
                    bufferedOutputStream.write("Visit Header".getBytes());
                    bufferedOutputStream.write('\n');
                    bufferedOutputStream.write('\n');
                    isFirstWriteHeader = false;
                }

                bufferedOutputStream.write("Version: ".getBytes());
                bufferedOutputStream.write(text.getBytes());
                bufferedOutputStream.write("\tID size: ".getBytes());
                bufferedOutputStream.write(idSize);
                bufferedOutputStream.write("\tTimestamp: ".getBytes());
                bufferedOutputStream.write(String.valueOf(timestamp).getBytes());
                bufferedOutputStream.write('\n');
                bufferedOutputStream.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        PMLog.e(TAG, "Version: %s", text);
        PMLog.e(TAG, "ID size: %d", idSize);

    }

    @Override
    public void visitStringRecord(ID id, String text, int timestamp, long length) {
        super.visitStringRecord(id, text, timestamp, length);
        if (bufferedOutputStream != null) {
            try {
                bufferedOutputStream.write("String Record\t".getBytes());
                bufferedOutputStream.write("ID : ".getBytes());
                bufferedOutputStream.write(id.getBytes());
                bufferedOutputStream.write('\t');
                bufferedOutputStream.write(id.toString().getBytes());
                bufferedOutputStream.write(", Text: ".getBytes());
                bufferedOutputStream.write(text.getBytes());
                bufferedOutputStream.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stringRecordMap.put(id.toString(), text);
//        PMLog.e(TAG, "String Record --- ID %s , Text: %s", id.toString(), text);
    }

    private HashMap<String, String> classRecordMap = new HashMap<>();

    @Override
    public void visitLoadClassRecord(int serialNumber, ID classObjectId, int stackTraceSerial, ID classNameStringId, int timestamp, long length) {
        super.visitLoadClassRecord(serialNumber, classObjectId, stackTraceSerial, classNameStringId, timestamp, length);
        try {
            bufferedOutputStream.write("Class Serial Num: ".getBytes());
            bufferedOutputStream.write(serialNumber);
            bufferedOutputStream.write(", classId: ".getBytes());
            bufferedOutputStream.write(classObjectId.getBytes());
            //class Name对应的string id
            bufferedOutputStream.write(", className String ID: ".getBytes());
            bufferedOutputStream.write(classNameStringId.getBytes());
            bufferedOutputStream.write(", stacktrace serial num: ".getBytes());
            bufferedOutputStream.write(stackTraceSerial);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String classNameStr = "";
        if (stringRecordMap.containsKey(classNameStringId.toString())) {
            classNameStr = stringRecordMap.get(classNameStringId.toString());
        }

        classRecordMap.put(classObjectId.toString(), classNameStr);

//        PMLog.e(TAG, "Class Record === " +
//                        "SerialNum: %d, " +
//                        "ClassObj ID: %s, " +
//                        "ClassName String Id: %s, " +
//                        "Stacktrace serialNum: %d",
//                serialNumber, classObjectId.toString(), classNameStr, stackTraceSerial);

    }

    @Override
    public void visitStackFrameRecord(ID id, ID methodNameId, ID methodSignatureId, ID sourceFileId, int serial, int lineNumber, int timestamp, long length) {
        super.visitStackFrameRecord(id, methodNameId, methodSignatureId, sourceFileId, serial, lineNumber, timestamp, length);
//        PMLog.e(TAG, "Stack Frame === " +
//                        "Stack frame id: %s, " +
//                        "method name string id: %s, " +
//                        "method sign string id: %s, " +
//                        "source file string id: %s, " +
//                        "class serialNum: %d, " +
//                        "lineNum: %d",
//                id.toString(), methodNameId.toString(), methodSignatureId.toString(), sourceFileId.toString(), serial, length);
    }

    @Override
    public void visitStackTraceRecord(int serialNumber, int threadSerialNumber, ID[] frameIds, int timestamp, long length) {
        super.visitStackTraceRecord(serialNumber, threadSerialNumber, frameIds, timestamp, length);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Stack Trace === Stacktrace serialNum: ")
                .append(serialNumber)
                .append(", thread serialNum: ")
                .append(threadSerialNumber)
                .append(", num of frames: ");

        if (frameIds.length > 0) {
            stringBuilder.append(frameIds.length)
                    .append('\n');
            for (int i = 0; i < frameIds.length; i++) {
                stringBuilder.append("frame[").append(i).append("]").append(frameIds[i].toString());
            }
        }

//        PMLog.e(TAG, stringBuilder.toString());
    }

    @Override
    public void visitThreadStartRecord(int threadSerialNumber, ID threadId, int stackTraceSerial, ID threadStrNameId, ID threadGroupNameId, ID threadParentGroupNameId) {
        super.visitThreadStartRecord(threadSerialNumber, threadId, stackTraceSerial, threadStrNameId, threadGroupNameId, threadParentGroupNameId);

        String threadName = "";
        if (stringRecordMap.containsKey(threadStrNameId.toString())) {
            threadName = stringRecordMap.get(threadStrNameId.toString());
        }

        PMLog.e(TAG, "visitThreadStartRecord === threadSerialNum: %d，threadId: %s, threadName: %s",
                threadSerialNumber, threadId.toString(), threadName);
    }

    @Override
    public void visitThreadEnd(int threadSerialNumber) {
        super.visitThreadEnd(threadSerialNumber);
        PMLog.e(TAG, "visitThreadEnd === threadSerialNum: %d",
                threadSerialNumber);
    }

    @Override
    public HprofHeapDumpVisitor visitHeapDumpRecord(int tag, int timestamp, long length) {
//        return super.visitHeapDumpRecord(tag, timestamp, length);
        return hprofHeapDumpVisitor;
    }

    @Override
    public void visitUnconcernedRecord(int tag, int timestamp, long length, byte[] data) {
        super.visitUnconcernedRecord(tag, timestamp, length, data);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        try {
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ID leakClassId;

    private HprofHeapDumpVisitor hprofHeapDumpVisitor = new HprofHeapDumpVisitor(null) {
        @Override
        public void visitHeapDumpInfo(int heapId, ID heapNameId) {
            super.visitHeapDumpInfo(heapId, heapNameId);
//            PMLog.e(TAG, "Heap Dump start read ... ");
        }

        @Override
        public void visitHeapDumpBasicObj(int tag, String tagInfo, ID id) {
            super.visitHeapDumpBasicObj(tag, tagInfo, id);
            if ("ROOT_VM_INTERNAL".equals(tagInfo) || "ROOT_INTERNED_STRING".equals(tagInfo))
                return;


            if ("ROOT_STICKY_CLASS".equals(tagInfo)) {
                String className = "";
                if (classRecordMap.containsKey(id.toString())) {
                    className = classRecordMap.get(id.toString());
                }
                if (className != null && className.contains("LeakActivity")) {
                    leakClassId = id;
                    PMLog.e(TAG, "Heap Dump: visitHeapDumpBasicObj ===tag: %s, Id: %s, className: %s",
                            tagInfo, id.toString(), className);
                }
            }

        }

        @Override
        public void visitHeapDumpJniLocal(ID id, int threadSerialNumber, int stackFrameNumber) {
            super.visitHeapDumpJniLocal(id, threadSerialNumber, stackFrameNumber);
        }

        @Override
        public void visitHeapDumpJavaFrame(ID id, int threadSerialNumber, int stackFrameNumber) {
            super.visitHeapDumpJavaFrame(id, threadSerialNumber, stackFrameNumber);
//            PMLog.e(TAG, "Heap Dump: visitHeapDumpJavaFrame === Id: %s, threadSerialNum: %d, stackFrameNumber: %d",
//                    id.toString(), threadSerialNumber, stackFrameNumber);
        }

        @Override
        public void visitHeapDumpNativeStack(ID id, int threadSerialNumber) {
            super.visitHeapDumpNativeStack(id, threadSerialNumber);
//            PMLog.e(TAG, "Heap Dump: visitHeapDumpNativeStack === Id: %s, threadSerialNum: %d",
//                    id.toString(), threadSerialNumber);
        }

        @Override
        public void visitHeapDumpThreadBlock(ID id, int threadSerialNumber) {
            super.visitHeapDumpThreadBlock(id, threadSerialNumber);
//            PMLog.e(TAG, "Heap Dump: visitHeapDumpThreadBlock === Id: %s, threadSerialNum: %d",
//                    id.toString(), threadSerialNumber);
        }

        @Override
        public void visitHeapDumpThreadObject(ID id, int threadSerialNumber, int stackFrameNumber) {
            super.visitHeapDumpThreadObject(id, threadSerialNumber, stackFrameNumber);
//            PMLog.e(TAG, "Heap Dump: visitHeapDumpThreadObject === Id: %s, threadSerialNum: %d, stackFrameNumber: %d",
//                    id.toString(), threadSerialNumber, stackFrameNumber);
        }

        @Override
        public void visitHeapDumpClass(ID id, int stackSerialNumber, ID superClassId, ID classLoaderId, int instanceSize, Field[] staticFields, Field[] instanceFields) {
            super.visitHeapDumpClass(id, stackSerialNumber, superClassId, classLoaderId, instanceSize, staticFields, instanceFields);
            if (leakClassId != null && leakClassId.toString().equals(id.toString())) {
                PMLog.e(TAG, "find leak class");

                if (classRecordMap.containsKey(superClassId.toString())) {
                    PMLog.e(TAG, "super class : %s", classRecordMap.get(superClassId.toString()));
                }

                if (classRecordMap.containsKey(classLoaderId.toString())) {
                    PMLog.e(TAG, "loader class : %s", classRecordMap.get(classLoaderId.toString()));
                }

                PMLog.e(TAG, "instanceSize: %d", instanceSize);

                for(int i=0; i< instanceFields.length;i++){
                    Field field = instanceFields[i];

                    PMLog.e(TAG, "instance Field === name: %s, type: %s",
                            stringRecordMap.get(field.nameId.toString()),
                           Type.getClassNameOfPrimitiveArray(Type.getType(field.typeId)));
                }

                for(int i=0; i< staticFields.length;i++){
                    Field field = staticFields[i];

                    PMLog.e(TAG, "static Field === name: %s, type: %s",
                            stringRecordMap.get(field.nameId.toString()),
                            Type.getClassNameOfPrimitiveArray(Type.getType(field.typeId)));
                }
            }
//            PMLog.e(TAG, "Heap Dump: visitHeapDumpThreadObject === Id: %s, threadSerialNum: %d, stackFrameNumber: %d",
//                    id.toString(), threadSerialNumber, stackFrameNumber);
        }

        @Override
        public void visitHeapDumpInstance(ID id, int stackId, ID typeId, byte[] instanceData) {
            super.visitHeapDumpInstance(id, stackId, typeId, instanceData);

            if(classRecordMap.containsKey(typeId.toString())){

            }

            PMLog.e(TAG, "dump instance === id: %s, class object: %s",
                    stringRecordMap.get(id.toString()),
                    stringRecordMap.get(typeId.toString()));
        }

        @Override
        public void visitHeapDumpObjectArray(ID id, int stackId, int numElements, ID typeId, byte[] elements) {
            super.visitHeapDumpObjectArray(id, stackId, numElements, typeId, elements);
        }

        @Override
        public void visitHeapDumpPrimitiveArray(int tag, ID id, int stackId, int numElements, int typeId, byte[] elements) {
            super.visitHeapDumpPrimitiveArray(tag, id, stackId, numElements, typeId, elements);
        }

        @Override
        public void visitHeapDumpJniMonitor(ID id, int threadSerialNumber, int stackDepth) {
            super.visitHeapDumpJniMonitor(id, threadSerialNumber, stackDepth);
        }


        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    };
}
