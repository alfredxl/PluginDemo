package com.alfredxl.javassist.sample;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/8/9 14:29
 */
public class Point {
    /*** 切入点对象本身(有可能为空,例如切的是静态方法) ***/
    private Object pointObject;
    /*** 切入方法的参数集 ***/
    private Object[] args;

    public Point() {
    }

    public Point(Object pointObject, Object[] args) {
        this.pointObject = pointObject;
        this.args = args;
    }

    public Object getPointObject() {
        return pointObject;
    }

    public void setPointObject(Object pointObject) {
        this.pointObject = pointObject;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder strb = new StringBuilder();
        strb.append(pointObject != null ? pointObject.toString() : "");
        strb.append("\n");
        if (args != null && args.length > 0) {
            for (Object item : args) {
                strb.append(item.toString());
                strb.append("\n");
            }
        }
        return strb.toString();
    }
}
