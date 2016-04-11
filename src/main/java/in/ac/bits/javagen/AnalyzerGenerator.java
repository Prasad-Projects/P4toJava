package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import in.ac.bits.javagen.mvc.Input;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@SuppressWarnings("restriction")
public class AnalyzerGenerator {

    @Autowired
    private EntityGenrator entityGenerator;

    @Autowired
    private GraphParser graphParser;

    @Autowired
    private BeautyParser beautyParser;

    private List<FieldSpec> headerVars;
    private List<String> headerFields;
    private Map<String, FieldSpec> fieldMap;
    private List<MethodSpec> methods;
    private Map<String, MethodSpec> getters;
    private String protocol;
    private Input input;
    private Map<String, Class> headerFieldTypeMap;

    private TypeSpec headerClass;

    private Map<String, String> analyzer = new HashMap<String, String>();
    private Map<String, String> analyzerProtocol = new HashMap<String, String>();

    private static final String classNameSuffix = "Analyzer";

    public void clearAll() {
        analyzer = new HashMap<String, String>();
        analyzerProtocol = new HashMap<String, String>();
    }

    private String capitalize(String str) {
        String firstChar = String.valueOf(str.charAt(0)).toUpperCase();
        str = str.replaceFirst(String.valueOf(str.charAt(0)), firstChar);
        return str;
    }

    private String decapitalize(String str) {
        String firstChar = String.valueOf(str.charAt(0)).toUpperCase();
        str = str.replaceFirst(String.valueOf(str.charAt(0)), firstChar);
        return str;
    }

    private String setClassName() {
        protocol = capitalize(protocol);
        return protocol + classNameSuffix;
    }

    public void generateAnalyzer() {

        fieldMap = new HashMap<String, FieldSpec>();
        methods = new ArrayList<MethodSpec>();

        String className = setClassName();
        TypeName eventBus = generateFields();

        // add configure method
        generateConfigure(eventBus);
        // add header setter
        generateHeaderSetter();
        // add startByte method
        generateStartByte();
        // add endByte method
        generateEndByte();
        // add publishTypedetectionevent method
        generatePublish();
        // generate getter methods
        generateGetters();
        // add analyze method
        generateAnalyze();
        // add next protocol detector
        generateNPType();

        ClassName customAnalyzer = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "CustomAnalyzer");
        ClassName scopeClass = ClassName.get(Scope.class);
        AnnotationSpec scope = AnnotationSpec.builder(scopeClass)
                .addMember("value", "\"prototype\"").build();

        List<FieldSpec> fields = new ArrayList<FieldSpec>(fieldMap.values());

        TypeSpec analyzerClass = TypeSpec.classBuilder(className)
                .addSuperinterface(customAnalyzer).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class).addAnnotation(scope)
                .addFields(fields).addMethods(methods).build();

        JavaFile javaFile = JavaFile
                .builder(input.getPackageName(), analyzerClass).build();
        File file = new File(input.getPath());

        try {
            System.out.println(
                    "Writing analyzer source file to location specified..");
            javaFile.writeTo(file);
            System.out.println(
                    "Finished writing analyzer source file to location specified!!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        analyzer.put(analyzerClass.name, input.getPackageName());
        analyzerProtocol.put(analyzerClass.name, protocol);

    }

    private void generateGetters() {

        graphParser.setInput(input);
        graphParser.parse(input.getGraphString());
        String conditionalField = graphParser.getConditionalHeaderField();

        beautyParser.setProtocol(protocol.toLowerCase());
        Map<String, String> beautyMap = beautyParser
                .parse(input.getBeautyString());

        // remove these if and else
        if (beautyMap == null) {
            System.out.println("Beauty map is null!!");
        } else {
            for (Entry<String, String> entry : beautyMap.entrySet()) {
                System.out.println(entry.getKey() + "::" + entry.getValue());
            }
        }

        ClassName beautify = ClassName.get("in.ac.bits.protocolanalyzer.utils",
                "Beautify");

        headerFieldTypeMap = new HashMap<String, Class>();
        getters = new HashMap<String, MethodSpec>();
        int fieldIndex = 0;
        ClassName byteOperator = ClassName
                .get("in.ac.bits.protocolanalyzer.utils", "ByteOperator");
        ClassName bitOperator = ClassName
                .get("in.ac.bits.protocolanalyzer.utils", "BitOperator");
        for (String field : headerFields) {
            System.out.println("Field=" + field);
            field = capitalize(field);
            ParameterSpec headerParam = ParameterSpec
                    .builder(byte[].class, protocol.toLowerCase() + "Header")
                    .build();
            @SuppressWarnings("rawtypes")
            Class returnType = determineReturnType(fieldIndex);
            com.squareup.javapoet.MethodSpec.Builder mbuilder = MethodSpec
                    .methodBuilder("get" + field).addModifiers(Modifier.PUBLIC)
                    .addParameter(headerParam)
                    .addStatement("$T " + field.toLowerCase()
                            + " = $T.parse($N, " + protocol + "Header."
                            + headerVars.get(4 * fieldIndex).name + ", "
                            + protocol + "Header."
                            + headerVars.get(4 * fieldIndex + 2).name + ")",
                            byte[].class, bitOperator, headerParam);

            if (beautyMap != null
                    && beautyMap.containsKey(field.toLowerCase())) {
                System.out.println("beauty map contains key :" + field);
                String mode = beautyMap.get(field.toLowerCase());
                mbuilder.addStatement("return $T.beautify("
                        + field.toLowerCase() + ", \"" + mode + "\")",
                        beautify);
                returnType = String.class;
            } else {
                if (!conditionalField.equalsIgnoreCase(field)) {
                    mbuilder.addStatement(
                            "$T returnVar = $T.parseBytes"
                                    + returnType.getName() + "("
                                    + field.toLowerCase() + ")",
                            returnType, byteOperator)
                            .addStatement("return returnVar");
                } else {
                    ClassName hexClass = ClassName
                            .get("org.apache.commons.codec.binary", "Hex");
                    returnType = String.class;
                    mbuilder.addStatement("return $T.encodeHexString("
                            + field.toLowerCase() + ")", hexClass);
                }
            }
            mbuilder.returns(returnType);
            MethodSpec method = mbuilder.build();
            methods.add(method);
            headerFieldTypeMap.put(field, returnType);
            System.out.println(
                    "Getter map: " + field.toLowerCase() + "::" + method.name);
            getters.put(field.toLowerCase(), method);
            fieldIndex++;
        }
    }

    @SuppressWarnings("rawtypes")
    private Class determineReturnType(int fieldIndex) {
        FieldSpec startBitField = headerVars.get(4 * fieldIndex);
        FieldSpec endBitField = headerVars.get(4 * fieldIndex + 2);
        int startBit = Integer.parseInt(startBitField.initializer.toString());
        int endBit = Integer.parseInt(endBitField.initializer.toString());
        return ReturnType.getReturnType(endBit - startBit + 1);
    }

    private void generateNPType() {

        CodeBlock cases = buildCases();

        String val = "\"NO_CONDITIONAL_HEADER_FIELD\"";
        if (!graphParser.getConditionalHeaderField().equalsIgnoreCase("NULL")) {
            MethodSpec getter = getters
                    .get(graphParser.getConditionalHeaderField());
            val = getter.name + "(this." + protocol.toLowerCase() + "Header)";
        }
        MethodSpec method = MethodSpec.methodBuilder("setNextProtocolType")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T nextHeaderType = " + val, String.class)
                .beginControlFlow("switch(nextHeaderType)").addCode(cases)
                .endControlFlow().returns(String.class).build();

        methods.add(method);
    }

    private CodeBlock buildCases() {
        com.squareup.javapoet.CodeBlock.Builder cbuilder = CodeBlock.builder();
        String conditionalHeader = graphParser.getConditionalHeaderField();
        if (!conditionalHeader.equalsIgnoreCase("NULL")) {
            for (Entry<String, String> entry : graphParser.getValProtocols()
                    .entrySet()) {
                cbuilder.addStatement(
                        "case \"" + entry.getKey() + "\"" + ": return Protocol."
                                + entry.getValue().toUpperCase());
            }
        }
        cbuilder.addStatement("default: return Protocol.END_PROTOCOL");
        CodeBlock cb = cbuilder.build();
        return cb;
    }

    private void generateAnalyze() {

        // subscribe
        ClassName sub = ClassName.get("com.google.common.eventbus",
                "Subscribe");
        AnnotationSpec subann = AnnotationSpec.builder(sub).build();

        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        ParameterSpec pw = ParameterSpec.builder(packetWrapper, "packetWrapper")
                .build();
        CodeBlock setNPublish = setNPublisher();
        CodeBlock setEntity = entitySetter();
        CodeBlock setQuery = querySetter();
        MethodSpec method = MethodSpec.methodBuilder("analyze")
                .addAnnotation(subann).addParameter(pw)
                .beginControlFlow(
                        "if ($N.equalsIgnoreCase($N.getPacketType()))",
                        fieldMap.get("relPkt"), pw)
                .addCode(setNPublish).addCode(setEntity).addCode(setQuery)
                .endControlFlow().addModifiers(Modifier.PUBLIC).build();
        methods.add(method);
    }

    private CodeBlock setNPublisher() {
        CodeBlock cb = CodeBlock.builder()
                .addStatement(
                        "set" + capitalize(protocol) + "Header(packetWrapper)")
                .addStatement("String nextPacketType = setNextProtocolType()")
                .addStatement("setStartByte(packetWrapper)")
                .addStatement("setEndByte(packetWrapper)")
                .addStatement(
                        "publishTypeDetectionEvent(nextPacketType, startByte, endByte)")
                .build();
        return cb;
    }

    private CodeBlock entitySetter() {
        // set the entity class first
        String entityPkgName = "in.ac.bits.protocolanalyzer.persistence.entity";
        entityGenerator.setInput(input);
        entityGenerator.setProtocol(protocol);
        entityGenerator.setHeaderFieldTypeMap(headerFieldTypeMap);
        entityGenerator.setPackageName(entityPkgName);
        List<FieldSpec> fields = entityGenerator.generateEntity();
        ClassName entityClass = ClassName.get(entityPkgName,
                protocol + "Entity");

        com.squareup.javapoet.CodeBlock.Builder cbuilder = CodeBlock.builder();
        cbuilder.addStatement("$T entity = new $T()", entityClass, entityClass);
        cbuilder.addStatement("entity.set" + capitalize(fields.get(0).name)
                + "(packetWrapper.getPacketId())");
        if (fields.size() > 1) {
            for (int i = 1; i < fields.size(); i++) {
                cbuilder.addStatement(
                        "entity.set" + capitalize(fields.get(i).name) + "(get"
                                + capitalize(fields.get(i).name) + "("
                                + protocol.toLowerCase() + "Header))");
            }
        }
        CodeBlock cb = cbuilder.build();
        return cb;
    }

    private CodeBlock querySetter() {

        ClassName indexQuery = ClassName.get(
                "org.springframework.data.elasticsearch.core.query",
                "IndexQuery");

        CodeBlock cb = CodeBlock.builder()
                .addStatement("$T query = new $T()", indexQuery, indexQuery)
                .addStatement("query.setObject(entity)")
                .addStatement("$N.save(query)", fieldMap.get("repo")).build();
        return cb;
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

    private void generateHeaderSetter() {
        ClassName packet = ClassName.get("org.pcap4j.packet", "Packet");
        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        ParameterSpec pw = ParameterSpec.builder(packetWrapper, "packetWrapper")
                .build();

        ClassName hClass = ClassName.get(input.getPackageName(),
                headerClass.name);
        MethodSpec method = MethodSpec
                .methodBuilder("set" + capitalize(protocol) + "Header")
                .addModifiers(Modifier.PRIVATE).addParameter(pw)
                .addStatement("$T packet = $N.getPacket()", packet, pw)
                .addStatement("int startByte = $N.getStartByte()", pw)
                .addStatement("byte[] rawPacket = packet.getRawData()")
                .addStatement(
                        "this." + protocol.toLowerCase()
                                + "Header = $T.copyOfRange(rawPacket, startByte, "
                                + "startByte + $T.TOTAL_HEADER_LENGTH" + ")",
                        Arrays.class, hClass)
                .build();
        methods.add(method);
    }

    private void generateEndByte() {
        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        MethodSpec eb = MethodSpec.methodBuilder("setEndByte")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(packetWrapper, "packetWrapper")
                .addStatement("this.endByte = packetWrapper.getEndByte()")
                .build();

        methods.add(eb);
    }

    private void generateStartByte() {

        ClassName packetWrapper = ClassName
                .get("in.ac.bits.protocolanalyzer.analyzer", "PacketWrapper");

        ClassName hClass = ClassName.get(input.getPackageName(),
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
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("$T." + protocol.toUpperCase(), protocolClass)
                .build();
        fieldMap.put("relPkt", relPkt);

        ClassName repoClass = ClassName.get(
                "in.ac.bits.protocolanalyzer.persistence.repository",
                "AnalysisRepository");
        Builder repoBuilder = FieldSpec
                .builder(repoClass.topLevelClassName(), "repository")
                .addAnnotation(Autowired.class).addModifiers(Modifier.PRIVATE);
        FieldSpec repo = repoBuilder.build();
        fieldMap.put("repo", repo);

        ClassName ebClass = ClassName.get("com.google.common.eventbus",
                "EventBus");
        Builder eventbusBuilder = FieldSpec
                .builder(ebClass.topLevelClassName(), "eventBus")
                .addModifiers(Modifier.PRIVATE);
        FieldSpec eb = eventbusBuilder.build();
        fieldMap.put("eb", eb);

        FieldSpec headerBytes = FieldSpec
                .builder(byte[].class, protocol.toLowerCase() + "Header")
                .addModifiers(Modifier.PRIVATE).build();
        fieldMap.put("headerBytes", headerBytes);
        FieldSpec startByte = FieldSpec.builder(int.class, "startByte")
                .addModifiers(Modifier.PRIVATE).build();
        fieldMap.put("startByte", startByte);
        FieldSpec endByte = FieldSpec.builder(int.class, "endByte")
                .addModifiers(Modifier.PRIVATE).build();
        fieldMap.put("endByte", endByte);

        return ebClass.topLevelClassName();
    }

}
