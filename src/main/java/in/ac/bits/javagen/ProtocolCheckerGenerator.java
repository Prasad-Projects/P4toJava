package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

@Component
public class ProtocolCheckerGenerator {

    @Autowired
    private AnalyzerGenerator generator;

    private Map<String, String> pkgProtocol;
    private Map<String, String> analyzerProtocol;

    private List<FieldSpec> fields;
    private List<MethodSpec> methods;

    public void generateChecker() {

        fields = new ArrayList<FieldSpec>();
        methods = new ArrayList<MethodSpec>();
        pkgProtocol = generator.getAnalyzer();
        analyzerProtocol = generator.getAnalyzerProtocol();

        // generate and autowire protocol field
        generateProtocolField();

        if (!pkgProtocol.isEmpty()) {
            generateCustom();
        } else {
            generateDefault();
        }
        TypeSpec checkerClass = TypeSpec.classBuilder("ProtocolChecker")
                .addAnnotation(Component.class).addModifiers(Modifier.PUBLIC)
                .addFields(fields).addMethods(methods).build();

        // change path later
        String path = "/home/mihirkakrambe/";
        String packageName = "in.ac.bits.protocolanalyzer.protocol";
        JavaFile javaFile = JavaFile.builder(packageName, checkerClass).build();
        File file = new File(path);

        try {
            System.out.println(
                    "Writing analyzer source file to location specified..");
            javaFile.writeTo(file);
            System.out.println(
                    "Finished writing analyzer source file to location specified!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateProtocolField() {
        ClassName protocolClass = ClassName
                .get("in.ac.bits.protocolanalyzer.protocol", "Protocol");
        FieldSpec protocol = FieldSpec.builder(protocolClass, "protocol")
                .addAnnotation(Autowired.class).addModifiers(Modifier.PRIVATE)
                .build();
        fields.add(protocol);
    }

    private void generateDefault() {
        System.out.println("Reached in generate default");
        addCheckNAdd(true);
    }

    private void generateCustom() {

        System.out.println("Reached in generate custom");
        for (Entry<String, String> entry : pkgProtocol.entrySet()) {
            String analyzer = entry.getKey().toLowerCase();
            String pkgName = entry.getValue();
            System.out.println("Pkgname received =" + pkgName);
            ClassName className = ClassName.get(pkgName, entry.getKey());
            FieldSpec analyzerField = FieldSpec.builder(className, analyzer)
                    .addAnnotation(Autowired.class)
                    .addModifiers(Modifier.PRIVATE).build();
            fields.add(analyzerField);
        }
        FieldSpec field = FieldSpec.builder(boolean.class, "defaultStatus")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("false").build();
        fields.add(field);
        addCheckNAdd(false);
    }

    private void addCheckNAdd(boolean defaultStatus) {
        Builder mbuilder = MethodSpec.methodBuilder("checkNAdd")
                .beginControlFlow("if (defaultStatus)").addCode(getIfBlock())
                .endControlFlow().addModifiers(Modifier.PUBLIC);
        if (!defaultStatus) {
            CodeBlock cb = getElseBlock();
            mbuilder.beginControlFlow("else").addCode(cb).endControlFlow();
        }
        methods.add(mbuilder.build());
    }

    private CodeBlock getIfBlock() {
        CodeBlock cb = CodeBlock.builder()
                .addStatement("protocol.defaultCustoms()").build();

        return cb;
    }

    private CodeBlock getElseBlock() {
        com.squareup.javapoet.CodeBlock.Builder cbuilder = CodeBlock.builder();

        for (Entry<String, String> entry : pkgProtocol.entrySet()) {
            String stage = String.valueOf(getStage(entry.getValue()));
            cbuilder.addStatement(
                    "protocol.addCustomAnalyzer(" + entry.getKey().toLowerCase()
                            + ", \"" + analyzerProtocol.get(entry.getKey())
                            + "\", " + stage + ")");
        }

        return cbuilder.build();
    }

    private int getStage(String pkgName) {
        if (StringUtils.containsIgnoreCase(pkgName, "link")) {
            return 1;
        } else if (StringUtils.containsIgnoreCase(pkgName, "network")) {
            return 2;
        } else {
            return 3;
        }
    }

    private String capitalize(String str) {
        String firstChar = String.valueOf(str.charAt(0)).toUpperCase();
        str = str.replaceFirst(String.valueOf(str.charAt(0)), firstChar);
        return str;
    }
}
