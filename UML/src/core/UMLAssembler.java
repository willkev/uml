package core;

import com.thoughtworks.qdox.model.JavaClass;
import java.util.Collection;

/**
 * @author Willian
 */
public class UMLAssembler {

    private String out;
    private final Collection<JavaClass> classes;
    private final boolean useListIgnore;
    private final Collection<String> classeslistIgnore;

    public UMLAssembler(Collection<JavaClass> classes, Collection<String> classeslistIgnore) {
        this.classes = classes;
        this.classeslistIgnore = classeslistIgnore;
        this.useListIgnore = !classeslistIgnore.isEmpty();
    }

    public String assemblyNormal() {
        assembly(null);
        return out;
    }

    /**
     * @param rootClass Se diferente de null, montará apenas as classes que
     * implementam a rootClass
     */
    public String assemblyDown(JavaClass rootClass) {
        assembly(rootClass);
        return out;
    }

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

}
