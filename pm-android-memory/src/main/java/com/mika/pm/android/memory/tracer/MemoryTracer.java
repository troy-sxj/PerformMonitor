package com.mika.pm.android.memory.tracer;

import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;

import com.mika.pm.android.core.util.DeviceUtil;
import com.mika.pm.android.core.util.MonitorHandlerThread;
import com.mika.pm.android.core.util.PMLog;

/**
 * @Author: mika
 * @Time: 2019-11-04 11:29
 * @Description: 定时采集内存使用情况，主要指标：
 * 1. 内存异常：PSS 超过 指定上限（暂定400MB，可以配置）
 * 2. 触顶：Java堆占用草果最大堆限制的85%
 */
public class MemoryTracer {

    private static final String Tag = "MemoryTracer";

    private Handler memoryHandler;
    private MemoryCollectTask collectTask;

    private boolean useArt;

    public MemoryTracer() {
        String vmVersion = System.getProperty("java.vm.version");
        useArt = vmVersion != null && vmVersion.startsWith("2");

        HandlerThread newHandlerThread = MonitorHandlerThread.getNewHandlerThread("Monitor#MemoryTracer");
        memoryHandler = new Handler(newHandlerThread.getLooper());
        collectTask = new MemoryCollectTask();

        memoryHandler.postDelayed(collectTask, 30 * 1000);
    }


    class MemoryCollectTask implements Runnable {

        private int overloadTimes;

        MemoryCollectTask() {
            overloadTimes = 3;
        }

        @Override
        public void run() {
            doCollect();
            memoryHandler.postDelayed(collectTask, 30 * 1000);
        }

        private void doCollect() {
            //1. 内存使用情况
            Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
            Debug.getMemoryInfo(memoryInfo);

            int totalPss = memoryInfo.getTotalPss();
            int totalUss = memoryInfo.getTotalPrivateClean() + memoryInfo.getTotalPrivateDirty();


            //2. 判断触顶
            long javaMax = Runtime.getRuntime().maxMemory();
            long javaTotal = Runtime.getRuntime().totalMemory();
            long javaUsed = javaTotal - Runtime.getRuntime().freeMemory();
            //Java 内存使用超过最大限制的85%
            float proportion = (float) javaUsed / javaMax;
            if(proportion > 0.8){
                overloadTimes --;
            }else{
                overloadTimes = 3;
            }

            //3. GC情况统计：需要特别注意阻塞式GC的次数和耗时，因为它会暂停应用线程，可能导致应用发生卡顿。
            String gcCount = "";
            String gcTotalTime = "";
            String blockGcCount = "";
            String blockGcTime = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useArt) { //art虚拟机
                // 运行的GC次数
                gcCount = Debug.getRuntimeStat("art.gc.gc-count");
                // GC使用的总耗时，单位是毫秒
                gcTotalTime = Debug.getRuntimeStat("art.gc.gc-time");
                // 阻塞式GC的次数
                blockGcCount = Debug.getRuntimeStat("art.gc.blocking-gc-count");
                // 阻塞式GC的总耗时
                blockGcTime = Debug.getRuntimeStat("art.gc.blocking-gc-time");
            } else {  //dalvik虚拟机
                //TODO dalvik gc信息获取
            }
            PMLog.i(Tag, "doCollect --------  %s",
                    printMemInfo(totalPss, totalUss, proportion, gcCount, gcTotalTime, blockGcCount, blockGcTime));

            if(overloadTimes == 0){
                //连续三次内存占用超过阀值，
                overloadTimes = 3;
            }
        }

        private String printMemInfo(int pss, int uss, float proportion,
                                    String gcCount, String gcTotalTime, String blockGcCount, String blockGcTime) {
            StringBuilder print = new StringBuilder();

            print.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Memory Info<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
            print.append("|* PSS: ").append(pss).append("\n");
            print.append("|* USS: ").append(uss).append("\n");
            print.append("|*\t[Use Info]\n");
            print.append("|*\t\tDalvikHeap: ").append(DeviceUtil.getDalvikHeap()).append("\n");
            print.append("|*\t\tNativeHeap: ").append(DeviceUtil.getNativeHeap()).append("\n");
            print.append("|*\t\tVmSize: ").append(DeviceUtil.getVmSize()).append("\n");
            print.append("|*\t\tUsed Rate: ").append(proportion).append("\n");
            print.append("|*\t[GC Info]\n");
            print.append("|*\t\tGC Count: ").append(gcCount).append("\n");
            print.append("|*\t\tGC Total Time: ").append(gcTotalTime).append("\n");
            print.append("|*\t\tBlock GC Count: ").append(blockGcCount).append("\n");
            print.append("|*\t\tBlock GC Total Time: ").append(blockGcTime).append("\n");
            print.append("=====================================================================\n");
            return print.toString();
        }
    }
}
