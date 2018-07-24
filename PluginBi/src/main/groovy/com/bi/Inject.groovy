package com.bi

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import org.apache.commons.io.FileUtils

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/6/29 10:57
 */
 class Inject {

     /**
      * 遍历该目录下的所有class，对所有class进行代码注入。
      * 其中以下class是不需要注入代码的：
      * --- 1. R文件相关
      * --- 2. 配置文件相关（BuildConfig）
      * --- 3. Application
      * @param path 目录的路径
      */
     static void injectDir(String path, ClassPool pool) {
         File dir = new File(path)
         if (dir.isDirectory()) {
             dir.eachFileRecurse { File file ->

                 String filePath = file.absolutePath
                 if (filePath.endsWith(".class")
                         && !filePath.contains('R$')
                         && !filePath.contains('R.class')
                         && !filePath.contains("BuildConfig.class")
                         // 这里是application的名字，可以通过解析清单文件获得，先写死了
                         && !filePath.contains("App.class")) {
                         int end = filePath.length() - 6 // .class = 6
                         String className = filePath.substring(0, end).replace('\\', '.').replace('/', '.')
                         injectClass(className, path, pool)
                 }
             }
         }
     }

     /**
      * 这里需要将jar包先解压，注入代码后再重新生成jar包
      * @path jar包的绝对路径
      */
     static void injectJar(String path, ClassPool pool) {
         if (path.endsWith(".jar")) {
             File jarFile = new File(path)

             // jar包解压后的保存路径
             String jarZipDir = jarFile.getParent() + "/" + jarFile.getName().replace('.jar', '')

             // 解压jar包, 返回jar包中所有class的完整类名的集合（带.class后缀）
             List classNameList = JarZipUtil.unzipJar(path, jarZipDir)

             // 删除原来的jar包
             jarFile.delete()

             // 注入代码
             pool.appendClassPath(jarZipDir)
             for (String className : classNameList) {
                 if (className.endsWith(".class")
                         && !className.contains('R$')
                         && !className.contains('R.class')
                         && !className.contains("BuildConfig.class")) {
                     className = className.substring(0, className.length() - 6)
                     injectClass(className, jarZipDir, pool)
                 }
             }

             // 从新打包jar
             JarZipUtil.zipJar(jarZipDir, path)

             // 删除目录
             FileUtils.deleteDirectory(new File(jarZipDir))
         }
     }

     private static void injectClass(String className, String path, ClassPool pool) {
         println("className  ~~ " + className)
         pool.clearImportedPackages()
         pool.importPackage("android.util.Log")
         CtClass c = pool.getCtClass(className)
         println("CtClass  ~~ " + c.name)
         if (c.isFrozen()) {
             c.defrost()
         }
         CtConstructor[] cts = c.getDeclaredConstructors()
         if (cts == null || cts.length == 0) {
             insertNewConstructor(c)
         } else {
             cts[0].insertBeforeBody("Log.i(\"javassist : constructor time = \", this.toString());")
         }
         c.writeFile(path)
         c.detach()
     }

     private static void insertNewConstructor(CtClass c) {
         CtConstructor constructor = new CtConstructor(new CtClass[0], c)
         constructor.setBody("Log.i(\"javassist : constructor time = \", this.toString());")
         c.addConstructor(constructor)
     }
}
