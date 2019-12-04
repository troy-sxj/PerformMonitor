package com.mika.pm.android.memory.hproflib;

import com.mika.pm.android.memory.hproflib.model.ID;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:19
 * @Description:
 */
public class HprofVisitor {

    protected HprofVisitor hv = null;

    public HprofVisitor(HprofVisitor hv) {
        this.hv = hv;
    }

    public void visitHeader(String text, int idSize, long timestamp) {
        if (this.hv != null) {
            this.hv.visitHeader(text, idSize, timestamp);
        }
    }

    public void visitStringRecord(ID id, String text, int timestamp, long length) {
        if (this.hv != null) {
            this.hv.visitStringRecord(id, text, timestamp, length);
        }
    }

    public void visitLoadClassRecord(int serialNumber, ID classObjectId, int stackTraceSerial, ID classNameStringId, int timestamp, long length) {
        if (this.hv != null) {
            this.hv.visitLoadClassRecord(serialNumber, classObjectId, stackTraceSerial, classNameStringId, timestamp, length);
        }
    }

    public void visitStackFrameRecord(ID id, ID methodNameId, ID methodSignatureId, ID sourceFileId, int serial, int lineNumber, int timestamp, long length) {
        if (this.hv != null) {
            this.hv.visitStackFrameRecord(id, methodNameId, methodSignatureId, sourceFileId, serial, lineNumber, timestamp, length);
        }
    }

    public void visitStackTraceRecord(int serialNumber, int threadSerialNumber, ID[] frameIds, int timestamp, long length) {
        if (this.hv != null) {
            this.hv.visitStackTraceRecord(serialNumber, threadSerialNumber, frameIds, timestamp, length);
        }
    }

    public void visitThreadStartRecord(int threadSerialNumber, ID threadId, int stackTraceSerial, ID threadStrNameId, ID threadGroupNameId, ID threadParentGroupNameId){
        if(this.hv != null){
            this.hv.visitThreadStartRecord(threadSerialNumber, threadId, stackTraceSerial, threadStrNameId, threadGroupNameId, threadParentGroupNameId);
        }
    }

    public void visitThreadEnd(int threadSerialNumber){
        if(this.hv != null){
            this.hv.visitThreadEnd(threadSerialNumber);
        }
    }

    public HprofHeapDumpVisitor visitHeapDumpRecord(int tag, int timestamp, long length) {
        if (this.hv != null) {
            return this.hv.visitHeapDumpRecord(tag, timestamp, length);
        } else {
            return null;
        }
    }

    public void visitUnconcernedRecord(int tag, int timestamp, long length, byte[] data) {
        if (this.hv != null) {
            this.hv.visitUnconcernedRecord(tag, timestamp, length, data);
        }
    }

    public void visitEnd() {
        if (this.hv != null) {
            this.hv.visitEnd();
        }
    }
}
