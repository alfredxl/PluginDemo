package com.bi

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <br> ClassName:   ${className}* <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/6/26 14:17
 */
class BiPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.logger.info("Bi-Start--------------")
        def android = project.extensions.findByType(AppExtension.class)
        android.registerTransform(new BiTransform(project))
    }
}
