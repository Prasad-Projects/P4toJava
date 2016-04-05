package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import in.ac.bits.javagen.mvc.Header;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class AnalyzerGenerator {

    private List<FieldSpec> headerFields;
    private List<FieldSpec> fields;
    private String protocol;
    private Header header;

    private static final String classNameSuffix = "Analyzer";

    private String setClassName(String protocol) {
        String firstChar = String.valueOf(protocol.charAt(0)).toUpperCase();
        protocol = protocol.replace(protocol.charAt(0), firstChar.charAt(0));
        return protocol + classNameSuffix;
    }

    public void generateAnalyzer(Header header) {

        fields = new ArrayList<FieldSpec>();

        String className = setClassName(protocol);
        TypeName eventBus = generateFields();
        MethodSpec configure = generateConfigureMethod(eventBus);

        TypeSpec analyzerClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC).addFields(fields)
                .addMethod(configure).build();

        JavaFile javaFile = JavaFile
                .builder(header.getPackageName(), analyzerClass).build();
        File file = new File(header.getPath());

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

    private MethodSpec generateConfigureMethod(TypeName eventBus) {
        MethodSpec configure = MethodSpec.methodBuilder("configure")
                .addParameter(eventBus, "eventBus")
                .addStatement("this.eventBus = eventBus")
                .addStatement("this.eventBus.register(this)")
                .addModifiers(Modifier.PUBLIC).build();
        return configure;
    }

    private TypeName generateFields() {
        ClassName repoClass = ClassName.get(
                "in.ac.bits.protocolanalyzer.persistence.repository",
                "AnalysisRepository");
        Builder repoBuilder = FieldSpec
                .builder(repoClass.topLevelClassName(), "repository")
                .addAnnotation(Autowired.class).addModifiers(Modifier.PRIVATE);
        FieldSpec repo = repoBuilder.build();
        fields.add(repo);

        ClassName ebClass = ClassName.get("com.google.common.eventbus",
                "EventBus");
        Builder eventbusBuilder = FieldSpec
                .builder(ebClass.topLevelClassName(), "eventBus")
                .addModifiers(Modifier.PRIVATE);
        FieldSpec eb = eventbusBuilder.build();
        fields.add(eb);

        FieldSpec headerBytes = FieldSpec
                .builder(byte[].class, protocol.toLowerCase() + "header")
                .addModifiers(Modifier.PRIVATE).build();
        fields.add(headerBytes);
        FieldSpec startByte = FieldSpec.builder(int.class, "startByte")
                .addModifiers(Modifier.PRIVATE).build();
        fields.add(startByte);
        FieldSpec endByte = FieldSpec.builder(int.class, "endByte")
                .addModifiers(Modifier.PRIVATE).build();
        fields.add(endByte);

        return ebClass.topLevelClassName();
    }

}
