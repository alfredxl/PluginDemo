package com.bi

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import com.android.build.api.transform.Format
import org.apache.commons.io.FileUtils


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
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        // inputs就是输入文件的集合
        // outputProvider可以获取outputs的路径
        // Transfrom的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
      /*  inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //TODO 这里可以对input的文件做处理，比如代码注入！
                Inject.injectDir(directoryInput.file.absolutePath)//调用方法进行注入
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                //TODO 这里可以对input的文件做处理，比如代码注入！
                String jarPath = jarInput.file.absolutePath
                String projectName = project.rootProject.name
                if (jarPath.endsWith("classes.jar")
                        && jarPath.contains("exploded-aar\\" + projectName)//这里的路径在我的项目中并不存在, gradle版本不同可能已经不一样了
                        // hotpatch module是用来加载dex，无需注入代码
                        && !jarPath.contains("exploded-aar\\" + projectName + "\\hotpatch")) {
                    Inject.injectJar(jarPath)//调用对jar进行注入的方法
                }
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // project.logger.error("dest = "+dest.absolutePath+"="+dest.exists())
                // project.logger.error("jarInput.file = "+jarInput.file.absolutePath+"="+jarInput.file.exists())
                dest.mkdirs()//需要先创建文件才可以哦
                dest.createNewFile()
                FileUtils.copyFile(jarInput.file, dest)
            }
        }*/
    }
}