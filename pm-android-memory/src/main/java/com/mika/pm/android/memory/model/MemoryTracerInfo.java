package com.mika.pm.android.memory.model;

/**
 * @Author: mika
 * @Time: 2019-11-04 15:11
 * @Description:
 */
public class MemoryTracerInfo {

    private int pss;
    private int vss;

    private long javaUsed;
    private long javaMax;
    private float javaUseRate;

    public static class GCInfo {
        String gcCount;
        String gcTotalTime;
        String blockGcCount;
        String blockGcTime;

        public String getGcCount() {
            return gcCount;
        }

        public void setGcCount(String gcCount) {
            this.gcCount = gcCount;
        }

        public String getGcTotalTime() {
            return gcTotalTime;
        }

        public void setGcTotalTime(String gcTotalTime) {
            this.gcTotalTime = gcTotalTime;
        }

        public String getBlockGcCount() {
            return blockGcCount;
        }

        public void setBlockGcCount(String blockGcCount) {
            this.blockGcCount = blockGcCount;
        }

        public String getBlockGcTime() {
            return blockGcTime;
        }

        public void setBlockGcTime(String blockGcTime) {
            this.blockGcTime = blockGcTime;
        }
    }
}
