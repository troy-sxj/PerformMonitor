package com.mika.pm.android.memory.config;

/**
 * @Author: mika
 * @Time: 2019-11-04 09:39
 * @Description:
 */
public class MemoryConfig {

    private boolean dumpHprof;
    private long collectInterval = 5 * 60;    //内存信息采集间隔，单位second. 默认5min
    private int maxRedetectTimes;

    public boolean isDumpHprof() {
        return dumpHprof;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public int getMaxRedetectTimes() {
        return maxRedetectTimes;
    }

    private MemoryConfig(){}

    public static class Builder{
        private MemoryConfig config = new MemoryConfig();

        public Builder enableDumpHprof(boolean enable){
            config.dumpHprof = enable;
            return this;
        }

        public Builder collectInterval(long second){
            config.collectInterval = second;
            return this;
        }

        public MemoryConfig build(){
            return config;
        }

    }
}
