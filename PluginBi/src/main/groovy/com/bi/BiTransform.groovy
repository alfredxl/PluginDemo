package com.bi

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPath
import javassist.ClassPool
import org.gradle.api.Project
import com.android.build.api.transform.Format
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.digest.DigestUtils


class BiTransform extends Transform {
    private Project project

    BiTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "BiTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        def pool = new ClassPool(ClassPool.getDefault())
        // 添加安卓包
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        List<ClassPath> listJarAndDirPath = new ArrayList<>()
        inputs.each { TransformInput input ->
            // 添加代码文件包
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dirPath = directoryInput.file.absolutePath
                pool.appendClassPath(dirPath)
            }
            // 添加所有的依赖包(否则会报找不到包)
            input.jarInputs.each { JarInput jarInput ->
                def jarPath = jarInput.file.absolutePath
                def jarClassPath = new JarClassPath(jarPath)
                pool.appendClassPath(jarClassPath)
                listJarAndDirPath.add(jarClassPath)
            }
        }

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                Inject.injectDir(directoryInput.file.absolutePath, pool)//调用方法进行注入\
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->

                //jar文件一般是第三方依赖库jar文件
                Inject.injectJar(jarInput.file.getAbsolutePath(), pool)
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        // 删除导入的jar
        listJarAndDirPath.each {
            pool.removeClassPath(it)
        }
        pool.clearImportedPackages()
        pool = null
        ClassPool.getDefault().clearImportedPackages()
    }
}