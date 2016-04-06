package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import in.ac.bits.javagen.mvc.Header;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class AnalyzerGenerator {

    private List<FieldSpec> headerVars;
    private List<String> headerFields;
    private List<FieldSpec> fields;
    private List<MethodSpec> methods;
    private String protocol;
    private Header header;

    private TypeSpec headerClass;

    private static final String classNameSuffix = "Analyzer";

    private String capitalize(String str) {
        String firstChar = String.valueOf(str.charAt(0)).toUpperCase();
        str = str.replaceFirst(String.valueOf(str.charAt(0)), firstChar);
        return str;
    }

    private String setClassName() {
        protocol = capitalize(protocol);
        return protocol + classNameSuffix;
    }

    public void generateAnalyzer() {

        fields = new ArrayList<FieldSpec>();
        methods = new ArrayList<MethodSpec>();

        String className = setClassName();
        TypeName eventBus = generateFields();

        // add configure method
        generateConfigure(eventBus);
        // add startByte method
        generateStartByte();
        // add endByte method
        generateEndByte();
        // add publishTypedetectionevent method
        generatePublish();
        // add analyze method
        generateAnalyze();
        // generate getter methods
        generateGetters();

        TypeSpec analyzerClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC).addFields(fields)
                .addMethods(methods).build();

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

    private void generateGetters() {

        int count = 0;
        for (String field : headerFields) {
            field = capitalize(field);
            ParameterSpec headerParam = ParameterSpec
                    .builder(byte[].class, protocol.toLowerCase() + "Header")
                    .build();
            MethodSpec method = MethodSpec.methodBuilder("get" + field)
                    .addModifiers(Modifier.PUBLIC).addParameter(headerParam)
                    .addStatement("$T " + field.toLowerCase()
                            + " = $T.copyOfRange($N, " + protocol + "Header."
                            + headerVars.get(4 * count + 1).name + ", "
                            + protocol + "Header."
                            + headerVars.get(4 * count + 3).name + " + 1)",
                            byte[].class, Arrays.class, headerParam)
                    .addStatement("return " + field.toLowerCase())
                    .returns(byte[].class)
                    .build();
            methods.add(method);
            count++;
        }
    }

    private void generateAnalyze() {

        // subscribe
        ClassName sub = ClassName.get("com.google.common.eventbus",
                "Subscribe");
        AnnotationSpec subann = AnnotationSpec.builder(sub).build();

        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        MethodSpec method = MethodSpec.methodBuilder("analyze")
                .addAnnotation(subann)
                .addParameter(packetWrapper, "packetWrapper")
                .addModifiers(Modifier.PUBLIC).build();
        methods.add(method);
    }

    private void generatePublish() {
        ClassName ptdEvent = ClassName.get(
                "in.ac.bits.protocolanalyzer.analyzer.event",
                "PacketTypeDetectionEvent");

        ParameterSpec npt = ParameterSpec
                .builder(String.class, "nextPacketType").build();
        ParameterSpec sb = ParameterSpec.builder(int.class, "startByte")
                .build();
        ParameterSpec eb = ParameterSpec.builder(int.class, "endByte").build();
        MethodSpec method = MethodSpec
                .methodBuilder("publishTypeDetectionEvent").addParameter(npt)
                .addParameter(sb).addParameter(eb)
                .addStatement("this.eventBus.post(new $T($N, $N, $N))",
                        ptdEvent, npt, sb, eb)
                .addModifiers(Modifier.PUBLIC).build();
        methods.add(method);
    }

    private void generateEndByte() {
        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        ClassName hClass = ClassName.get(header.getPackageName(),
                headerClass.name);
        MethodSpec eb = MethodSpec.methodBuilder("setEndByte")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(packetWrapper, "packetWrapper")
                .addStatement("this.endByte = packetWrapper.getEndByte()")
                .build();

        methods.add(eb);
    }

    private void generateStartByte() {

        FieldSpec totalLen = headerVars.get(headerVars.size() - 1);

        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        ClassName hClass = ClassName.get(header.getPackageName(),
                headerClass.name);
        MethodSpec sb = MethodSpec.methodBuilder("setStartByte")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(packetWrapper, "packetWrapper")
                .addStatement(
                        "this.startByte = packetWrapper.getStartByte() + $T.TOTAL_HEADER_LENGTH",
                        hClass)
                .build();

        methods.add(sb);
    }

    private void generateConfigure(TypeName eventBus) {
        MethodSpec configure = MethodSpec.methodBuilder("configure")
                .addParameter(eventBus, "eventBus")
                .addStatement("this.eventBus = eventBus")
                .addStatement("this.eventBus.register(this)")
                .addModifiers(Modifier.PUBLIC).build();
        methods.add(configure);
    }

    private TypeName generateFields() {

        ClassName protocolClass = ClassName
                .get("in.ac.bits.protocolanalyzer.protocol", "Protocol");
        FieldSpec relPkt = FieldSpec
                .builder(String.class, "PACKET_TYPE_OF_RELEVANCE")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T." + protocol.toUpperCase(), protocolClass)
                .build();
        fields.add(relPkt);

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
                .builder(byte[].class, protocol.toLowerCase() + "Header")
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
