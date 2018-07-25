package com.bi

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod

/**
 * <br> ClassName:   ${className}* <br> Description:
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
                    // 截取包目录
                    filePath = filePath.substring(dir.absolutePath.length() + 1)
                    String className = filePath.substring(0, filePath.length() - 6)
                            .replace(File.separator, '.')
                    println("dirPath : " + filePath + "\n className : " + className)
                    injectClass(className, path, pool)
                }
            }
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
//        CtConstructor[] cts = c.getDeclaredConstructors()
//        if (cts == null || cts.length == 0) {
////            insertNewConstructor(c)
//        } else {
//            cts[0].insertBeforeBody("Log.i(\"javassist : constructor time = \", this.toString());")
//        }
        CtMethod method = c.getDeclaredMethod("onCreate")
        if (method != null) {
            method.insertAfter("Log.i(\"javassist : constructor time = \", this.toString());")
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
