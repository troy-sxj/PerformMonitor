package com.mika.pm.android.memory.config;

/**
 * @Author: mika
 * @Time: 2019-11-04 09:39
 * @Description:
 */
public class MemoryConfig {

    private boolean dumpHprof;

    public boolean isDumpHprof() {
        return dumpHprof;
    }

    private MemoryConfig(){}

    public static class Builder{
        private MemoryConfig config = new MemoryConfig();

        public Builder enableDumpHprof(boolean enable){
            config.dumpHprof = enable;
            return this;
        }

        public MemoryConfig build(){
            return config;
        }

    }
}
