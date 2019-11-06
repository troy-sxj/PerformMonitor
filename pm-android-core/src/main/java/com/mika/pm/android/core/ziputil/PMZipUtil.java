package com.mika.pm.android.core.ziputil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: mika
 * @Time: 2019-11-05 18:07
 * @Description:
 */
public class PMZipUtil {
    private static final int BUFFER_SIZE = 16384;

    public static void extractTinkerEntry(PMZipFile apk, PMZipEntry zipEntry, PMZipOutputStream outputStream) throws IOException {
        InputStream in = null;
        try {
            in = apk.getInputStream(zipEntry);
            outputStream.putNextEntry(new PMZipEntry(zipEntry));
            byte[] buffer = new byte[BUFFER_SIZE];

            for (int length = in.read(buffer); length != -1; length = in.read(buffer)) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static void extractTinkerEntry(PMZipEntry zipEntry, InputStream inputStream, PMZipOutputStream outputStream) throws IOException {
        outputStream.putNextEntry(zipEntry);
        byte[] buffer = new byte[BUFFER_SIZE];

        for (int length = inputStream.read(buffer); length != -1; length = inputStream.read(buffer)) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.closeEntry();
    }

    public static void extractLargeModifyFile(PMZipEntry sourceArscEntry, File newFile, long newFileCrc, PMZipOutputStream outputStream) throws IOException {
        PMZipEntry newArscZipEntry = new PMZipEntry(sourceArscEntry);

        newArscZipEntry.setMethod(PMZipEntry.STORED);
        newArscZipEntry.setSize(newFile.length());
        newArscZipEntry.setCompressedSize(newFile.length());
        newArscZipEntry.setCrc(newFileCrc);
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(newFile));
            outputStream.putNextEntry(new PMZipEntry(newArscZipEntry));
            byte[] buffer = new byte[BUFFER_SIZE];

            for (int length = in.read(buffer); length != -1; length = in.read(buffer)) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
