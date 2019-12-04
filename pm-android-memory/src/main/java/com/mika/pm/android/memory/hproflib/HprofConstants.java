package com.mika.pm.android.memory.hproflib;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:22
 * @Description:
 */
public final class HprofConstants {
    public static final int RECORD_TAG_UNKNOWN = 0x0;
    public static final int RECORD_TAG_STRING = 0x1;
    public static final int RECORD_TAG_LOAD_CLASS = 0x2;
    public static final int RECORD_TAG_UNLOAD_CLASS = 0x3;
    public static final int RECORD_TAG_STACK_FRAME = 0x4;
    public static final int RECORD_TAG_STACK_TRACE = 0x5;
    public static final int RECORD_TAG_ALLOC_SITES = 0x6;
    public static final int RECORD_TAG_HEAP_SUMMARY = 0x7;
    public static final int RECORD_TAG_START_THREAD = 0xa;
    public static final int RECORD_TAG_END_THREAD = 0xb;
    public static final int RECORD_TAG_HEAP_DUMP = 0xc;
    public static final int RECORD_TAG_HEAP_DUMP_SEGMENT = 0x1c;
    public static final int RECORD_TAG_HEAP_DUMP_END = 0x2c;
    public static final int RECORD_TAG_CPU_SAMPLES = 0xd;
    public static final int RECORD_TAG_CONTROL_SETTINGS = 0xe;

    public static final int HEAPDUMP_ROOT_UNKNOWN = 0xff;

    public static final int HEAPDUMP_ROOT_JNI_GLOBAL = 0x1; //native中的全局变量
    public static final int HEAPDUMP_ROOT_JNI_LOCAL = 0x2;  //native代码中的本地变量，例如 user defined JNI code or JVM internal code
    public static final int HEAPDUMP_ROOT_JAVA_FRAME = 0x3; //本地变量，例如线程栈帧中的参数和方法
    public static final int HEAPDUMP_ROOT_NATIVE_STACK = 0x4;
    public static final int HEAPDUMP_ROOT_STICKY_CLASS = 0x5;   //被bootstrap/system class 加载器加载的类，例如所有rt.jar中包名为 java.uti.* 的类
    public static final int HEAPDUMP_ROOT_THREAD_BLOCK = 0x6;   //
    public static final int HEAPDUMP_ROOT_MONITOR_USED = 0x7;   //所有调用wait()、notify()方法的，或者同步的。例如：调用 synchronized(object) 或者进入一个synchronized method
    public static final int HEAPDUMP_ROOT_THREAD_OBJECT = 0x8;
    public static final int HEAPDUMP_ROOT_CLASS_DUMP = 0x20;
    public static final int HEAPDUMP_ROOT_INSTANCE_DUMP = 0x21;
    public static final int HEAPDUMP_ROOT_OBJECT_ARRAY_DUMP = 0x22;
    public static final int HEAPDUMP_ROOT_PRIMITIVE_ARRAY_DUMP = 0x23;
    /* Android tags */
    public static final int HEAPDUMP_ROOT_HEAP_DUMP_INFO = 0xfe;
    public static final int HEAPDUMP_ROOT_INTERNED_STRING = 0x89;
    public static final int HEAPDUMP_ROOT_FINALIZING = 0x8a;    //在finalizer等待队列里的对象
    public static final int HEAPDUMP_ROOT_DEBUGGER = 0x8b;
    public static final int HEAPDUMP_ROOT_REFERENCE_CLEANUP = 0x8c;
    public static final int HEAPDUMP_ROOT_VM_INTERNAL = 0x8d;
    public static final int HEAPDUMP_ROOT_JNI_MONITOR = 0x8e;
    public static final int HEAPDUMP_ROOT_UNREACHABLE = 0x90;  /* deprecated */
    public static final int HEAPDUMP_ROOT_PRIMITIVE_ARRAY_NODATA_DUMP = 0xc3;

    private HprofConstants() {
        throw new UnsupportedOperationException();
    }
}
