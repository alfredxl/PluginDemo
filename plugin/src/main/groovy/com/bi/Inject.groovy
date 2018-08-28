package com.bi

import com.google.common.io.ByteStreams
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import org.apache.commons.io.FileUtils
import sun.rmi.runtime.Log

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
    private static Map<String, Class> map = new HashMap<>()

    private static Class getAnnotationClass(String className, ClassPool mClassPool) {
        if (!map.containsKey(className)) {
            CtClass mCtClass = mClassPool.getCtClass(className)
            if (mCtClass.isFrozen()) {
                mCtClass.defrost()
            }
            map.put(className, mCtClass.toClass())
            mCtClass.detach()
        }
        return map.get(className)
    }

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
                            && checkIsARouterMethod(fileName)) {
                        FileInputStream inputStream = new FileInputStream(file)
                        FileOutputStream outputStream = new FileOutputStream(outPutFile)
                        transform(inputStream, outputStream, mClassPool)
                    } else {
                        FileUtils.copyFile(file, outPutFile)
                    }
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
                    if (!entry.isDirectory() && checkIsARouterMethod(fileName))
                        transform(zis, zos, mClassPool)
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
//        CtMethod[] methods = c.getDeclaredMethods("toString")
//        if (methods != null && methods.length > 0) {
//            CtMethod item = methods[0]
//            if (item != null && checkMethod(item.getModifiers()) && !item.isEmpty()) {
//                item.insertBefore("System.out.println(\"javassist : toString time = \" + " + c.name + ".class.getSimpleName());")
//            }
//        }
        CtMethod[] methods = c.getDeclaredMethods()
        if (methods != null && methods.length > 0) {
            for (CtMethod item : methods) {
                if (item != null && checkMethod(item.getModifiers())) {
                    Class a = getAnnotationClass("com.alfredxl.javassist.sample.PointAnnotation", mClassPool)
                    Object object = null
                    try {
                        object = item.getAnnotation(a)
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace()
                    }
                    if (object != null) {
                        String className = a.getMethod("className").invoke(object)
                        String methodName = a.getMethod("methodName").invoke(object)
                        item.insertBefore(className + "." + methodName + "(new com.alfredxl.javassist.sample.Point(\$0, \$args));")
                    }
                }
            }
        }
    }


    private static boolean checkMethod(int modifiers) {
        return !Modifier.isStatic(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isEnum(modifiers) && !Modifier.isInterface(modifiers)
    }


    private boolean checkIsARouterMethod(String fileName){
        Log.isAnnotationPresent()
//        if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
//            // This one of root elements, load root.
//            ((IRouteRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.groupsIndex);
//        } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
//            // Load interceptorMeta
//            ((IInterceptorGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.interceptorsIndex);
//        } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
//            // Load providerIndex
//            ((IProviderGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(Warehouse.providersIndex);
//        }
    }

}
