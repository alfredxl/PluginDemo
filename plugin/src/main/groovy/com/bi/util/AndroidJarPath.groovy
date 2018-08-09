package com.bi.util

import org.gradle.api.Project

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/25 14:58
 */
class AndroidJarPath {

    static String getPath(Project project){
        return project.android.bootClasspath[0].toString()
    }
}
