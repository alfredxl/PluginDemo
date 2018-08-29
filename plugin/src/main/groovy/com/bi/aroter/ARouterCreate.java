package com.bi.aroter;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/8/28 14:49
 */
public class ARouterCreate {
    private static ARouterCreate mARouterCreate;
    private CtClass c;
    private File arouterOut;
    private List<String> groupsList = new ArrayList<>();
    private List<String> interceptorsList = new ArrayList<>();
    private List<String> providersList = new ArrayList<>();

    private ARouterCreate() {
    }

    public static ARouterCreate getInstance() {
        if (mARouterCreate == null) {
            mARouterCreate = new ARouterCreate();
        }
        return mARouterCreate;
    }

    public static void reSet() {
        mARouterCreate = null;
    }

    public void createClass(File arouterOut, ClassPool mClassPool) {
        this.arouterOut = arouterOut;
        c = mClassPool.makeClass("com.alibaba.android.arouter.core.ARouterPathUtil");
        c.setModifiers(Modifier.PUBLIC);
        try {
            CtMethod ctMethod = CtNewMethod.make("public static void init() {}", c);
            c.addMethod(ctMethod);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    public void addGroupsIndex(String className) {
        groupsList.add(className);
    }

    public void addInterceptorsIndex(String className) {
        interceptorsList.add(className);
    }

    public void addProvidersIndex(String className) {
        providersList.add(className);
    }

    public void writeToFile() throws NotFoundException, CannotCompileException, IOException {
        if (arouterOut.isFile()) {
            arouterOut.delete();
            arouterOut.mkdirs();
        }
        if (!arouterOut.exists()) {
            arouterOut.mkdirs();
        }
        writeMethod();
        c.writeFile(arouterOut.getAbsolutePath());
    }

    private void writeMethod() throws NotFoundException {
        CtMethod method = c.getDeclaredMethod("init");
        for (String item : groupsList) {
            try {
                method.insertAfter(String.format("new com.alibaba.android.arouter.routes.%s().loadInto(com.alibaba.android.arouter.core.Warehouse.groupsIndex);", item));
                System.out.println("ARouter 注入: router : " + item);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
        for (String item : interceptorsList) {
            try {
                method.insertAfter(String.format("new com.alibaba.android.arouter.routes.%s().loadInto(com.alibaba.android.arouter.core.Warehouse.interceptorsIndex);", item));
                System.out.println("ARouter 注入: router : " + item);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
        for (String item : providersList) {
            try {
                method.insertAfter(String.format("new com.alibaba.android.arouter.routes.%s().loadInto(com.alibaba.android.arouter.core.Warehouse.providersIndex);", item));
                System.out.println("ARouter 注入: router : " + item);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
    }
}
