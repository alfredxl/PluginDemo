package com.bi

import com.bi.aroter.ARouterCreate
import com.google.common.io.ByteStreams
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * <br> ClassName:   ${className}* <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/6/29 10:57
 */
class Inject {
    public static final String SDK_NAME = "ARouter"
    public static final String SEPARATOR = "\$\$"
    public static final String SUFFIX_ROOT = "Root"
    public static final String SUFFIX_INTERCEPTORS = "Interceptors"
    public static final String SUFFIX_PROVIDERS = "Providers"
    public static final String DOT = "."
    public static final String ROUTE_ROOT_PAKCAGE = "com.alibaba.android.arouter.routes"

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    static void injectDir(String inputPath, String outPutPath, ClassPool mClassPool) {
        File dir = new File(inputPath)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.isFile()) {
                    File outPutFile = new File(outPutPath + filePath.substring(inputPath.length()))
                    Files.createParentDirs(outPutFile)
                    if (filePath.endsWith(".class")
                            && checkLogisticsCenter(filePath)) {
                        FileInputStream inputStream = new FileInputStream(file)
                        FileOutputStream outputStream = new FileOutputStream(outPutFile)
                        transform(inputStream, outputStream, mClassPool)
                    } else {
                        FileUtils.copyFile(file, outPutFile)
                    }
                    checkIsARouterMethod(filePath)
                }
            }
        }
    }

    static void injectJar(String jarInPath, String jarOutPath, ClassPool mClassPool) throws IOException {
        ArrayList entries = new ArrayList()
        Files.createParentDirs(new File(jarOutPath))
        FileInputStream fis = null
        ZipInputStream zis = null
        FileOutputStream fos = null
        ZipOutputStream zos = null
        try {
            fis = new FileInputStream(new File(jarInPath))
            zis = new ZipInputStream(fis)
            fos = new FileOutputStream(new File(jarOutPath))
            zos = new ZipOutputStream(fos)
            ZipEntry entry = zis.getNextEntry()
            while (entry != null) {
                String fileName = entry.getName()
                if (!entries.contains(fileName)) {
                    entries.add(fileName)
                    zos.putNextEntry(new ZipEntry(fileName))
                    if (!entry.isDirectory() && checkLogisticsCenter(fileName))
                        transform(zis, zos, mClassPool)
                    else {
                        ByteStreams.copy(zis, zos)
                    }
                    checkIsARouterMethod(fileName)
                }
                entry = zis.getNextEntry()
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (zos != null)
                zos.close()
            if (fos != null)
                fos.close()
            if (zis != null)
                zis.close()
            if (fis != null)
                fis.close()
        }
    }


    static void transform(InputStream input, OutputStream out, ClassPool mClassPool) {
        try {
            CtClass c = mClassPool.makeClass(input)
            play(c, mClassPool)
            out.write(c.toBytecode())
            c.detach()
        } catch (Exception e) {
            e.printStackTrace()
            input.close()
            out.close()
            throw new RuntimeException(e.getMessage())
        }
    }

    private static void play(CtClass c, ClassPool mClassPool) {
        if (c.isFrozen()) {
            c.defrost()
        }
        CtMethod[] methods = c.getDeclaredMethods("init")
        if (methods != null && methods.length > 0) {
            CtMethod init = methods[0]
            init.setBody("{mContext = \$1;\nexecutor = \$2;\ncom.alibaba.android.arouter.core.ARouterPathUtil.init();}")
            println("com.alibaba.android.arouter.core.LogisticsCenter.class insert success")
        }
    }


    private static void checkIsARouterMethod(String fileName) {
        if (fileName.endsWith(".class")) {
            fileName = fileName.substring(0, fileName.length() - 6)
            String classNamePath = fileName.replace("\\", ".").replace("/", ".")
            String className = classNamePath.substring(classNamePath.lastIndexOf(".") + 1)
            if (classNamePath.contains(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                ARouterCreate.getInstance().addGroupsIndex(className)
            } else if (classNamePath.contains(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                ARouterCreate.getInstance().addInterceptorsIndex(className)
            } else if (classNamePath.contains(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                ARouterCreate.getInstance().addProvidersIndex(className)
            }
        }
    }

    private static boolean checkLogisticsCenter(String fileName) {
        return fileName.replace("\\", ".").replace("/", ".").endsWith("com.alibaba.android.arouter.core.LogisticsCenter.class")
    }

}
