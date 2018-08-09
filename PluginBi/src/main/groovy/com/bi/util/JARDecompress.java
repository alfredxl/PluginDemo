package com.bi.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/25 16:27
 */


public class JARDecompress {
    /**
     * <br> Description: 压缩文件
     * <br> Author:      xwl
     * <br> Date:        2018/7/25 16:28
     *
     * @param jarFileName jarFileName
     * @param outputPath  outputPath
     */
    public static void doIt(String jarFileName, String outputPath) {
        try {
            // 执行解压
            decompress(jarFileName, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压缩JAR包
     *
     * @param fileName   文件名
     * @param outputPath 解压输出路径
     * @throws IOException IO异常
     */
    private static void decompress(String fileName, String outputPath)
            throws IOException {
        //创建解压包路径
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
        //创建jar文件对象引用
        JarFile jf = new JarFile(fileName);
        //枚举jar文件中的每个元素
        for (Enumeration e = jf.entries(); e.hasMoreElements(); ) {
            JarEntry je = (JarEntry) e.nextElement(); //jar中元素条目
            String outFileName = outputPath + je.getName(); //解压文件的名称
            File f = new File(outFileName); //创建解压文件对象
            // 创建该路径的目录和所有父目录
            createFatherDir(outFileName);
            // 如果是目录，则直接进入下一个循环
            if (f.isDirectory()) {
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = jf.getInputStream(je);
                FileOutputStream fos = new FileOutputStream(f);
                out = new BufferedOutputStream(fos);
                byte[] buffer = new byte[2048];
                int nBytes = 0;
                while ((nBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, nBytes);
                }
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                try {
                    if (out != null) {
                        out.flush(); //立刻写
                        out.close();
                    }
                } catch (IOException ioe) {
                    throw ioe;
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        }

    }

    /**
     * 循环创建父目录，正则表达式方式
     *
     * @param outFileName
     */
    private static void createFatherDir(String outFileName) {
        // 匹配分隔符
        Pattern p = Pattern.compile("[/\\" + File.separator + "]");
        Matcher m = p.matcher(outFileName);
        // 每找到一个匹配的分隔符，则创建一个该分隔符以前的目录
        while (m.find()) {
            int index = m.start();
            String subDir = outFileName.substring(0, index);
            File subDirFile = new File(subDir);
            if (!subDirFile.exists()) {
                subDirFile.mkdir();
            }
        }
    }
}
