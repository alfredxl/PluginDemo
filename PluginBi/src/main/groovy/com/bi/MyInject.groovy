package com.bi

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/2 16:45
 */
class MyInject {
    private static ClassPool pool = ClassPool.getDefault()
    private static String injectStr = "System.out.println(\"I Love HuaChao\" ); "

    static void inject(String path, String packageName) {
        pool.appendClassPath(path)
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.absolutePath
                //确保当前文件是class文件，并且不是系统自动生成的class文件
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")) {
                    // 判断当前目录是否是在我们的应用包里面
                    int index = filePath.indexOf(packageName)
                    boolean isMyPackage = index != -1
                    if (isMyPackage) {
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end)
                                .replace('\\', '.').replace('/', '.')
                        //开始修改class文件
                        CtClass c = pool.getCtClass(className)

                        if (c.isFrozen()) {
                            c.defrost()
                        }

//                        CtConstructor[] cts = c.getDeclaredConstructors()
//                        if (cts == null || cts.length == 0) {
//                            //手动创建一个构造函数
//                            CtConstructor constructor = new CtConstructor(new CtClass[0], c)
//                            constructor.insertBeforeBody(injectStr)
//                            c.addConstructor(constructor)
//                        } else {
//                            cts[0].insertBeforeBody(injectStr)
//                        }

                        if (c.name.contains("MainActivity")) {
                            for (int i = 0; i < c.declaredMethods.size(); i++) {
                                def method = c.declaredMethods[i]
                                println(method.name)
                                if (method.name.contains("toast")){
                                    method.insertAfter("Toast.makeText(this, \"cccc\", Toast.LENGTH_SHORT).show();")
                                    println("插入成功")//测试成功的插入代码
                                }
                            }
                        }

                        c.writeFile(path)
                        c.detach()
                    }
                }
            }
        }
    }
}
