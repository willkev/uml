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

    public static final String PATH = Config.getString("workstation.root");
    private List<String> myPackges = null;
    private JavaProjectBuilder builder = null;
    private Collection<String> classesName = null;
    private Collection<JavaClass> classes = null;
    private Collection<String> classeslistIgnore = null;
    private Collection<String> innersIgnore = null;
    public boolean useListIgnore = false;

    public Uml() throws Exception {
        File pathDir = new File(PATH);
        if (!pathDir.exists() || !pathDir.isDirectory()) {
            throw new Exception(pathDir + " não existe ou não é um diretório!");
        }
        DirectoryScanner scan = new DirectoryScanner(pathDir);
        scan.addFilter(new SuffixFilter(".java"));
        System.out.println("[Scan: " + scan.scan().size() + "] " + PATH);
        builder = new JavaProjectBuilder();
        builder.setEncoding("UTF-8");
        int testClasses = 0;
        int ignore = 0;
        int error = 0;
        for (File javaFile : scan.scan()) {
            try {
                // Ignora as classes de teste (testa no padrão Win e Unix)
                if (javaFile.getPath().contains("src\\test\\java")
                        || javaFile.getPath().contains("src/test/java")) {
                    testClasses++;
                    continue;
                }
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
        System.out.printf("[Tests(Ignore): %d]\n", testClasses);
        System.out.printf("[Loaded: %d]\n", builder.getSources().size());
        myPackges = new ArrayList<String>();
        for (JavaPackage jPck : builder.getPackages()) {
            myPackges.add(jPck.getName());
        }
    }

    private void init() {
        classesName = new HashSet<String>();
        classes = new HashSet<JavaClass>();
        classeslistIgnore = new HashSet<String>();
        innersIgnore = new HashSet<String>();
    }

    public void findAllRecursive(JavaClass java) {
        init();
        try {
            Uml.this.findAllRecursive(java, true);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        assemblyNormal();
    }

    public void findAll(JavaClass java) {
        init();
        try {
            Uml.this.findAllRecursive(java, false);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        for (JavaClass sun : java.getDerivedClasses()) {
            if (classesName.add(sun.getFullyQualifiedName())) {
                classes.add(sun);
            }
        }
        assemblyNormal();
    }

    public void findDown(JavaClass java) {
        init();
//        classesName.add(java.getFullyQualifiedName());
//        classes.add(java);
        for (JavaClass sun : java.getDerivedClasses()) {
            if (classesName.add(sun.getFullyQualifiedName())) {
                classes.add(sun);
            }
        }
        assemblyDown(java);
    }

    public void findUp(JavaClass java) {
        init();
        try {
            Uml.this.findAllRecursive(java, false);
        } catch (Exception ex) {
            PopUp.error(ex);
            return;
        }
        assemblyNormal();
    }
    private String out;

    private void assemblyNormal() {
        assembly(null);
    }

    private void assemblyDown(JavaClass rootClass) {
        assembly(rootClass);
    }

    /**
     * @param rootClass Se diferente de null, montará apenas as classes que implementam a rootClass
     */
    private void assembly(JavaClass rootClass) {
        out = "@startuml\n";
        for (JavaClass jc : classes) {
            assemblyDeclare(jc);
        }
        for (JavaClass jc : classes) {
            assemblySuperClass(jc);
        }
        // Assembler Interfaces
        for (JavaClass jc : classes) {
            if (!jc.getInterfaces().isEmpty()) {
                for (JavaClass jcInterface : jc.getInterfaces()) {
                    if (rootClass != null) {
                        if (!rootClass.getFullyQualifiedName().equals(jcInterface.getFullyQualifiedName())) {
                            continue;
                        }
                    }
                    assemblyInterface(jc, jcInterface);
                }
            }
        }
        CtrlC.copy(out);
    }

    private void assemblyDeclare(JavaClass jc) {
        if (jc.isInterface()) {
            out += "interface " + jc.getName() + "\n";
        } else if (jc.isAbstract()) {
            out += "abstract " + jc.getName() + "\n";
        } else {
            out += "class " + jc.getName() + "\n";
        }
    }

    private void assemblySuperClass(JavaClass jc) {
        if (jc.getSuperClass() == null) {
            return;
        }
        if (!useListIgnore) {
            out += jc.getSuperJavaClass().getName() + " <|-- " + jc.getName() + "\n";
            return;
        }
        if (!classeslistIgnore.contains(jc.getSuperJavaClass().getFullyQualifiedName())) {
            out += jc.getSuperJavaClass().getName() + " <|-- " + jc.getName() + "\n";
        }
    }

    private void assemblyInterface(JavaClass jc, JavaClass jcInterface) {
        out += jcInterface.getName() + " <.. " + jc.getName() + "\n";
    }

    private void findAllRecursive(JavaClass java, boolean findAllWays) throws Exception {
        if (java.isInner()) {
            innersIgnore.add(java.getFullyQualifiedName());
            return;
        }
        if (findAllWays) {
            for (JavaClass sun : java.getDerivedClasses()) {
                if (!classesName.contains(sun.getFullyQualifiedName())) {
                    Uml.this.findAllRecursive(sun, findAllWays);
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
                classes.add(java);
            }
            Uml.this.findAllRecursive(java.getSuperJavaClass(), findAllWays);
        }
        if (classesName.add(java.getFullyQualifiedName())) {
            classes.add(java);
        }
        List<JavaClass> interfaces = java.getImplementedInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
            for (JavaClass jc : interfaces) {
                Uml.this.findAllRecursive(jc, findAllWays);
            }
        }
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