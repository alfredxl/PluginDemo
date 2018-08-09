package com.bi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPath;
import javassist.NotFoundException;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/7/24 14:41
 */
public class JarClassPath implements ClassPath {
    JarFile jarfile;
    String jarfileURL;

    public JarClassPath(String pathname) throws NotFoundException {
        try {
            jarfile = new JarFile(pathname);
            jarfileURL = new File(pathname).getCanonicalFile()
                    .toURI().toURL().toString();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NotFoundException(pathname);
    }

    @Override
    public InputStream openClassfile(String classname)
            throws NotFoundException {
        try {
            String jarname = classname.replace('.', '/') + ".class";
            JarEntry je = jarfile.getJarEntry(jarname);
            if (je != null) {
                return jarfile.getInputStream(je);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NotFoundException("broken jar file?: "
                + jarfile.getName());
    }

    @Override
    public URL find(String classname) {
        String jarname = classname.replace('.', '/') + ".class";
        JarEntry je = jarfile.getJarEntry(jarname);
        if (je != null) {
            try {
                return new URL("jar:" + jarfileURL + "!/" + jarname);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void close() {
        try {
            jarfile.close();
            jarfile = null;
        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
        return jarfile == null ? "<null>" : jarfile.toString();
    }
}
