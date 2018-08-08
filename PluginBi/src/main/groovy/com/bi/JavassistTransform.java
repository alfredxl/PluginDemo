package com.bi;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;


/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/25 14:43
 */
public class JavassistTransform extends Transform {
    private Project project;

    public JavassistTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return "JavassistTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws IOException {
        System.out.println("JavassistTransform_start...");
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        // 删除上次编译目录
        outputProvider.deleteAll();
        try {
            // 添加android包
            for (TransformInput input : inputs) {
                // 对所有自己编写的代码进行复制
                for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    // 获取output目录
                    File dest = outputProvider.getContentLocation(directoryInput.getName(),
                            directoryInput.getContentTypes(), directoryInput.getScopes(),
                            Format.DIRECTORY);
                    // 处理
                    System.out.println("perform_directory : " + dest.getAbsolutePath());
                    Inject.injectDir(directoryInput.getFile().getAbsolutePath(), dest.getAbsolutePath(), project);
                }

                // 用于保存jar文件夹路径
                // 复制所有的jar，并解压缩文件
                for (JarInput jarInput : input.getJarInputs()) {
                    // 重命名输出文件（同目录copyFile会冲突）
                    String jarName = jarInput.getName();
                    String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4);
                    }
                    //生成输出路径
                    File dest = outputProvider.getContentLocation(jarName + md5Name,
                            jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                    // 处理
                    System.out.println("perform_jar : " + jarInput.getFile().getAbsolutePath());
                    Inject.injectJar(jarInput.getFile().getAbsolutePath(), dest.getAbsolutePath(), project);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Inject.clearClassPool();
        System.out.println("JavassistTransform_end...");
    }
}
