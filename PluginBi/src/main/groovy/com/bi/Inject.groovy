package com.bi

import com.bi.util.AndroidJarPath
import com.google.common.io.ByteStreams
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

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

    private static ClassPool mClassPool

    private static ClassPool getClassPool(Project project) {
        if (mClassPool == null) {
            mClassPool = new ClassPool(ClassPool.getDefault())
            mClassPool.appendClassPath(AndroidJarPath.getPath(project))
        }
        return mClassPool
    }

    static void clearClassPool() {
        mClassPool = null
    }

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    static void injectDir(String inputPath, String outPutPath, Project project) {
        File dir = new File(inputPath)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.isFile()) {
                    File outPutFile = new File(outPutPath + filePath.substring(inputPath.length()))
                    Files.createParentDirs(outPutFile)
                    if (filePath.endsWith(".class")
                            && !filePath.contains('R$')
                            && !filePath.contains('R.class')
                            && !filePath.contains("BuildConfig.class")) {
                        FileInputStream inputStream = new FileInputStream(file)
                        FileOutputStream outputStream = new FileOutputStream(outPutFile)
                        transform(inputStream, outputStream, project)
                    } else {
                        FileUtils.copyFile(file, outPutFile)
                    }
                }
            }
        }
    }

    static void transform(InputStream input, OutputStream out, Project project) {
        try {
            CtClass c = getClassPool(project).makeClass(input)
            if (c.isFrozen()) {
                c.defrost()
            }
            CtMethod[] methods = c.getDeclaredMethods("toString")
            if (methods != null && methods.length > 0) {
                CtMethod item = methods[0]
                if (item != null && checkMethod(item.getModifiers()) && !item.isEmpty()) {
                    item.insertBefore("System.out.println(\"javassist : constructor time = \" + " + c.name + ".class.getSimpleName());")
                }
            }
            out.write(c.toBytecode())
            c.detach()
        } catch (Exception e) {
            e.printStackTrace()
            input.close()
            out.close()
            throw new RuntimeException(e.getMessage())
        }
    }

    static void injectJar(String jarInPath, String jarOutPath, Project project) throws IOException {
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
                    if (!entry.isDirectory() && fileName.endsWith(".class")
                            && !fileName.contains('R$')
                            && !fileName.contains('R.class')
                            && !fileName.contains("BuildConfig.class"))
                        transform(zis, zos, project)
                    else {
                        ByteStreams.copy(zis, zos)
                    }
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

    private static boolean checkMethod(int modifiers) {
        return Modifier.isPublic(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isEnum(modifiers) && !Modifier.isInterface(modifiers)
    }


}
