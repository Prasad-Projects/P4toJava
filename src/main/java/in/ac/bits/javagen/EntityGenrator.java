package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import in.ac.bits.javagen.mvc.Input;
import lombok.Setter;

@Component
public class EntityGenrator {

    private List<FieldSpec> fields;

    @Setter
    private String protocol;

    @Setter
    private String packageName;

    @Setter
    private Input input;

    @Setter
    private Map<String, Class> headerFieldTypeMap;

    public List<FieldSpec> generateEntity() {

        fields = new ArrayList<FieldSpec>();

        // generate packetid field
        generatePacketId();

        // generate header fields
        generateHeaderFields();

        ClassName esdoc = ClassName.get(
                "org.springframework.data.elasticsearch.annotations",
                "Document");

        ClassName getter = ClassName.get("lombok", "Getter");
        ClassName setter = ClassName.get("lombok", "Setter");
        TypeSpec entityClass = TypeSpec.classBuilder(protocol + "Entity")
                .addModifiers(Modifier.PUBLIC).addAnnotation(getter)
                .addAnnotation(setter).addFields(fields)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, entityClass).build();
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
        return fields;
    }

    private void generatePacketId() {
        FieldSpec pid = FieldSpec.builder(long.class, "packetId")
                .addModifiers(Modifier.PRIVATE).build();
        fields.add(pid);

    }

    private void generateHeaderFields() {
        Set<String> headerFields = headerFieldTypeMap.keySet();
        List<Class> keys = new ArrayList<Class>(headerFieldTypeMap.values());

        int count = 0;
        for (String fieldName : headerFields) {
            FieldSpec field = FieldSpec
                    .builder(keys.get(count), decapitalize(fieldName))
                    .addModifiers(Modifier.PRIVATE).build();
            fields.add(field);
            count++;
        }
    }

    private String decapitalize(String str) {
        String firstChar = String.valueOf(str.charAt(0)).toLowerCase();
        str = str.replaceFirst(String.valueOf(str.charAt(0)), firstChar);
        return str;
    }

}
