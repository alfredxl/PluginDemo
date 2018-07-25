package com.bi.util


/**
 * <br> ClassName:   ${className}* <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/6/29 11:04
 */
class JarZipUtil {
    static void deleteFile(File file) {
        if (file.isDirectory()) {
            file.listFiles().each {
                if (it.isDirectory()) {
                    deleteFile(it)
                } else {
                    it.delete()
                }
            }
            file.delete()
        } else {
            file.delete()
        }
    }

}
