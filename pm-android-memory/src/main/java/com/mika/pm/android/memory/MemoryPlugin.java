package com.mika.pm.android.memory;

import com.mika.pm.android.core.plugin.Plugin;
import com.mika.pm.android.memory.config.MemoryConfig;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:28
 * @Description:
 */
public class MemoryPlugin extends Plugin {

    private MemoryConfig mConfig;

    public MemoryPlugin(MemoryConfig config) {
        this.mConfig = config;
    }
}
