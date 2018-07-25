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
import com.bi.util.AndroidJarPath;
import com.bi.util.JARCompress;
import com.bi.util.JARDecompress;
import com.bi.util.JarZipUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/25 14:43
 */
public class BiTransform extends Transform {
    private Project project;

    public BiTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return "BiTransform";
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
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        // 删除上次编译目录
        outputProvider.deleteAll();
        // 定义ClassPool
        ClassPool pool = new ClassPool(ClassPool.getDefault());
        try {
            // 添加android包
            pool.appendClassPath(AndroidJarPath.getPath(project));
            // 用于保存多次循环后的jar解压后的路径(用于最后删除)
            List<String> jarClassPathAll = new ArrayList<>();
            for (TransformInput input : inputs) {
                // 复制后的副本路径
                List<String> dirClassPathList = new ArrayList<>();
                // 对所有自己编写的代码进行复制
                for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    // 获取output目录
                    File dest = outputProvider.getContentLocation(directoryInput.getName(),
                            directoryInput.getContentTypes(), directoryInput.getScopes(),
                            Format.DIRECTORY);
                    // 将input的目录复制到output指定目录
                    FileUtils.copyDirectory(directoryInput.getFile(), dest);
                    // 将复制的代码文件的路径添加到集合
                    dirClassPathList.add(dest.getAbsolutePath());
                    // 将复制的代码文件路径添加到索引类工具包（便于后去查找类）
                    pool.appendClassPath(dest.getAbsolutePath());
                }

                // 用于保存解压后的jar文件夹路径，便于重新打包
                List<String> jarClassPath = new ArrayList<>();
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

 /*                   /////////jar方案还有问题,暂时不修改jar只做拷贝////////start
                    // 解压缩文件夹路径
                    String dirZipPath = dest.getAbsolutePath().replace(".jar", "");
                    JARDecompress.doIt(jarInput.getFile().getAbsolutePath(), dirZipPath);
                    // 将解压后的路径存储到集合
                    jarClassPath.add(dirZipPath);
                    // 将解压后的文件路径添加到索引类工具包
                    pool.appendClassPath(dirZipPath);
                    /////////jar方案还有问题,暂时不修改jar只做拷贝////////end*/

                    /////替代方案////拷贝
                    FileUtils.copyFile(jarInput.getFile(), dest);
                    pool.appendClassPath(new JarClassPath(jarInput.getFile().getAbsolutePath()));
                    /////替代方案////拷贝

                }
                // 把jar集合添加到总集合中;
                jarClassPathAll.addAll(jarClassPath);
                // 对自己编写的代码进行插入操作
                for (String filePath : dirClassPathList) {
                    Inject.injectDir(filePath, pool);
                }
                //  对第三方的已经解压的文件进行插入操作
                for (String jarPath : jarClassPath) {
                    Inject.injectDir(jarPath, pool);
                    // 重新打包成jar
                    JARCompress.doIt(jarPath, jarPath + ".jar");
                }
            }
            for (String jarPath : jarClassPathAll) {
                // 删除jar解包后的文件夹
                JarZipUtil.deleteFile(new File(jarPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool.clearImportedPackages();
        ClassPool.getDefault().clearImportedPackages();
    }
}
