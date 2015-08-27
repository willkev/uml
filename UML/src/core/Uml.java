package core;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.directorywalker.DirectoryScanner;
import com.thoughtworks.qdox.directorywalker.SuffixFilter;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.parser.ParseException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.management.BadAttributeValueExpException;
import resources.Config;
import utils.CtrlC;
import utils.PopUp;

public class Uml {

    private static final String PATH = Config.getString("workstation.root");

    private final JavaProjectBuilder builder = new JavaProjectBuilder();
    private final List<String> myPackges = new ArrayList<>();
    private final Collection<String> classesName = new HashSet();
    private final Collection<JavaClass> classes = new HashSet();
    private final Collection<String> classeslistIgnore = new HashSet();
    private final Collection<String> innersIgnore = new HashSet();

    public boolean useTestClasses = false;
    public boolean useListIgnore = false;

    public Uml() throws Exception {
        File pathDir = new File(PATH);
        if (!pathDir.exists() || !pathDir.isDirectory()) {
            throw new Exception(pathDir + " não existe ou não é um diretório!");
        }
        DirectoryScanner scan = new DirectoryScanner(pathDir);
        scan.addFilter(new SuffixFilter(".java"));
        System.out.println("[Scan: " + scan.scan().size() + "] " + PATH);
        builder.setEncoding("UTF-8");
        int ignore = 0;
        int error = 0;
        for (File javaFile : scan.scan()) {
            try {
                builder.addSource(javaFile);
                System.out.println(builder.getSources().size() + " [Load-ok]" + javaFile.getPath());
            } catch (Exception ex) {
                if (ex instanceof ParseException) {
                    ignore++;
                    System.out.println("[Ignore] " + javaFile.getPath());
                } else {
                    error++;
                    System.out.println("[ERROR] " + javaFile.getPath());
                }
            }
        }
        System.out.printf("[Error: %d]\n", error);
        System.out.printf("[Ignore: %d]\n", ignore);
        System.out.printf("[Loaded: %d]\n", builder.getSources().size());
        for (JavaPackage jPck : builder.getPackages()) {
            myPackges.add(jPck.getName());
        }
    }

    private void init() {
        classesName.clear();
        classes.clear();
        classeslistIgnore.clear();
        innersIgnore.clear();
    }

    public void findAllRecursive(JavaClass java) {
        init();
        try {
            findAllRecursive(java, true);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        UMLAssembler assembler = new UMLAssembler(classes, classeslistIgnore);
        String UMLassembled = assembler.assemblyNormal();
        CtrlC.copy(UMLassembled);
    }

    public void findAll(JavaClass java) {
        init();
        try {
            findAllRecursive(java, false);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        for (JavaClass sun : java.getDerivedClasses()) {
            if (classesName.add(sun.getFullyQualifiedName())) {
                addClass(sun);
            }
        }
        UMLAssembler assembler = new UMLAssembler(classes, classeslistIgnore);
        String UMLassembled = assembler.assemblyNormal();
        CtrlC.copy(UMLassembled);
    }

    public void findDown(JavaClass java) {
        init();
//        classesName.add(java.getFullyQualifiedName());
//        addClass(java);
        for (JavaClass sun : java.getDerivedClasses()) {
            if (classesName.add(sun.getFullyQualifiedName())) {
                addClass(sun);
            }
        }
        UMLAssembler assembler = new UMLAssembler(classes, classeslistIgnore);
        String UMLassembled = assembler.assemblyDown(java);
        CtrlC.copy(UMLassembled);
    }

    public void findUp(JavaClass java) {
        init();
        try {
            findAllRecursive(java, false);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        UMLAssembler assembler = new UMLAssembler(classes, classeslistIgnore);
        String UMLassembled = assembler.assemblyNormal();
        CtrlC.copy(UMLassembled);
    }

    private void findAllRecursive(JavaClass java, boolean findAllWays) throws Exception {
        if (java.isInner()) {
            innersIgnore.add(java.getFullyQualifiedName());
            return;
        }
        if (findAllWays) {
            for (JavaClass sun : java.getDerivedClasses()) {
                if (!classesName.contains(sun.getFullyQualifiedName())) {
                    findAllRecursive(sun, findAllWays);
                }
            }
        }
        if (!myPackges.contains(java.getPackageName())) {
            classeslistIgnore.add(java.getFullyQualifiedName());
            return;
        }
        if (!java.isInterface()) {
            if (java.getSuperJavaClass() == null) {
                throw new Exception("Structure error!");
            }
            if (classesName.add(java.getFullyQualifiedName())) {
                addClass(java);
            }
            findAllRecursive(java.getSuperJavaClass(), findAllWays);
        }
        if (classesName.add(java.getFullyQualifiedName())) {
            addClass(java);
        }
        List<JavaClass> interfaces = java.getImplementedInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
            for (JavaClass jc : interfaces) {
                findAllRecursive(jc, findAllWays);
            }
        }
    }

    private void addClass(JavaClass java) {
        // Se não deve levar em conta as classes de teste
        if (!useTestClasses && java.getSource().getURL() != null) {
            // Ignora as classes de teste (testa no padrão Win e Unix)
            if (java.getSource().getURL().toString().contains("src\\test\\java")
                    || java.getSource().getURL().toString().contains("src/test/java")) {
                return;
            }
        }
        classes.add(java);
    }

    public JavaClass get(String findClass) throws BadAttributeValueExpException {
        if (findClass == null || findClass.isEmpty()) {
            throw new BadAttributeValueExpException("Null or Empty!");
        }
        JavaClass javaReturn = null;
        for (JavaClass java : builder.getClasses()) {
            if (java.getFullyQualifiedName().endsWith(findClass)) {
                if (javaReturn == null) {
                    javaReturn = java;
                } else {
                    throw new BadAttributeValueExpException("More than one: " + findClass);
                }
            }
        }
        if (javaReturn == null) {
            throw new BadAttributeValueExpException("Not found: " + findClass);
        }
        return javaReturn;
    }
}
