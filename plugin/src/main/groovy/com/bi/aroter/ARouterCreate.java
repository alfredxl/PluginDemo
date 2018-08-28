package com.bi.aroter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

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
    private ClassPool mClassPool;

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
        this.mClassPool = mClassPool;
        c = mClassPool.makeClass("com.alibaba.android.arouter.core.ARouterPathUtil");
        c.setModifiers(Modifier.PUBLIC);
    }

    public void writeToFile(File arouterOut) {
        try {
            if (arouterOut.isFile()) {
                arouterOut.delete();
                arouterOut.mkdirs();
            }
            if (!arouterOut.exists()) {
                arouterOut.mkdirs();
            }
            c.writeFile(arouterOut.getAbsolutePath());
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
