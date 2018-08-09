package com.bi.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/25 16:24
 */
public class JARCompress {
    /**
     * jar压缩功能测试.
     *
     * @param dir     所要压缩的目录名（包含绝对路径）
     * @param jarName 压缩后的文件名
     * @throws Exception e
     */
    public static void doIt(String dir, String jarName) throws Exception {
        File folderObject = new File(dir);
        if (folderObject.exists()) {
            List fileList = getSubFiles(new File(dir));
            // 压缩文件名
            FileOutputStream fos = new FileOutputStream(jarName);
            JarOutputStream zos = new JarOutputStream(fos);
            JarEntry ze = null;
            byte[] buf = new byte[1024];
            int readLen = 0;
            for (int i = 0; i < fileList.size(); i++) {
                File f = (File) fileList.get(i);
                ze = new JarEntry(getAbsFileName(dir, f));
                ze.setSize(f.length());
                ze.setTime(f.lastModified());

                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
                is.close();
            }
            zos.close();
        } else {
            throw new Exception("文件夹不存在!");
        }
    }

    /**
     * 取得指定目录以及其各级子目录下的所有文件的列表，注意，返回的列表中不含目录.
     *
     * @param baseDir File 指定的目录
     * @return 包含java.io.File的List
     */
    private static List<File> getSubFiles(File baseDir) {
        List<File> fileList = new ArrayList<>();
        File[] tmp = baseDir.listFiles();
        if (tmp != null) {
            for (File tempFile : tmp) {
                if (tempFile.isFile()) {
                    fileList.add(tempFile);
                } else {
                    fileList.addAll(getSubFiles(tempFile));
                }
            }
        }
        return fileList;
    }

    /**
     * 给定根目录及文件的实际路径，返回带有相对路径的文件名，用于zip文件中的路径.
     * 如将绝对路径，baseDir\dir1\dir2\file.txt改成 dir1/dir2/file.txt
     *
     * @param baseDir      java.lang.String 根目录
     * @param realFileName java.io.File 实际的文件名
     * @return 相对文件名
     */
    private static String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        while (true) {
            real = real.getParentFile();
            if (real == null) {
                break;
            }
            if (real.equals(base)) {
                break;
            } else {
                ret = real.getName() + "/" + ret;
            }
        }
        return ret;
    }
}
