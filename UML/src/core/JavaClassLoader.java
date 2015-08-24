/*
 * @copyright Copyright (c) 2015 Animati Sistemas de Informática Ltda.
 * (http://www.animati.com.br)
 */
package core;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.directorywalker.DirectoryScanner;
import com.thoughtworks.qdox.directorywalker.SuffixFilter;
import com.thoughtworks.qdox.model.JavaClass;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Willian Kirschner (willian@animati.com.br)
 * @version 2015 Ago, 24
 */
public class JavaClassLoader {

    public static ArrayList<Class> getClasses(Class clazz) {
        return getClasses0(clazz, null, false, true);
    }

    public static ArrayList<Class> getClasses(Class clazz, String sufixClassName, boolean ignoreTest, boolean ignoreOwnClass) {
        return getClasses0(clazz, sufixClassName, ignoreTest, ignoreOwnClass);
    }

    // ignoreOwnClass - Ignora a própria 'Clazz'
    private static ArrayList<Class> getClasses0(Class clazz, String sufixJavaClassName, boolean ignoreTest, boolean ignoreOwnClass) {
        // Caminho da pasta da Workstation
        File workstationRoot = new File(System.getProperty("user.dir").split("animati-communicator")[0]);
        System.out.printf("Buscando Listeners em '%s'\n", workstationRoot.getAbsolutePath());
        DirectoryScanner scan = new DirectoryScanner(workstationRoot);
        if (sufixJavaClassName == null) {
            scan.addFilter(new SuffixFilter(".java"));
        } else {
            scan.addFilter(new SuffixFilter(sufixJavaClassName + ".java"));
        }
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.setEncoding("UTF-8");
        for (File javaFile : scan.scan()) {
            try {
                if (ignoreTest) {
                    // Ignora as classes de teste (testa no padrão Win e Unix)
                    if (javaFile.getPath().contains("src\\test\\java")
                            || javaFile.getPath().contains("src/test/java")) {
                        continue;
                    }
                }
                builder.addSource(javaFile);
            } catch (Exception ex) {
                System.out.printf("Error in addSource(javaFile) %s\n", ex.getMessage());
            }
        }
        System.out.printf("Count Sorces=%d\n", builder.getSources().size());
        ArrayList<Class> listeners = new ArrayList<>();
        // Filtra pelas classes que herdam AnimatiListener
        for (JavaClass java : builder.getClasses()) {
            // Verifica se a classe herda AnimatiListener
            if (java.isA(clazz.getName())) {
                // Se deve ignorar a própria Classe e for a própria classe
                if (ignoreOwnClass && clazz.getName().equals(java.getFullyQualifiedName())) {
                    continue;
                }
                try {
                    listeners.add(Class.forName(java.getFullyQualifiedName()));
                } catch (Exception ex) {
                    System.out.printf("Error in Class.forName() %s\n", ex.getMessage());
                }
            }
        }
        return listeners;
    }

}
