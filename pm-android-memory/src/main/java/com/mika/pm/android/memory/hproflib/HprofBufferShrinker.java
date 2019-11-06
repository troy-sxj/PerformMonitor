package com.mika.pm.android.memory.hproflib;

import com.mika.pm.android.core.util.PMUtil;
import com.mika.pm.android.memory.hproflib.model.Field;
import com.mika.pm.android.memory.hproflib.model.ID;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    private final Set<ID> mBmBUfferIds = new HashSet<>();
    private final Map<ID, byte[]> mBufferIdToElementDataMap = new HashMap<>();
    private final Map<ID, ID> mBufferIdToDeduplicatedIdMap = new HashMap<>();
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

        is = new FileInputStream(hprofIn);
        os = new BufferedOutputStream(new FileOutputStream(hprofOut));
    }
}
