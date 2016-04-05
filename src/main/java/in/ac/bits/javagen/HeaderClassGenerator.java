package in.ac.bits.javagen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.stereotype.Component;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@Component
public class HeaderClassGenerator {

    private List<FieldSpec> fieldSpecs;

    private String className;
    private String path;
    private String packageName;

    private TypeSpec genClass;

    public TypeSpec getHeaderClass() {
        return this.genClass;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldSpec> generateHeaderClass(List<String> fields,
            List<Integer> startBits, int offset) {

        fieldSpecs = new ArrayList<FieldSpec>();
        addFields(fields, startBits, offset);

        FieldSpec totalLen = createTotalLenField();

        TypeSpec headerClass = TypeSpec.classBuilder(this.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs).addField(totalLen).build();

        JavaFile javaFile = JavaFile.builder(packageName, headerClass).build();
        File file = new File(path);

        try {
            System.out.println("Writing java file to location specified..");
            javaFile.writeTo(file);
            System.out.println(
                    "Finished writing java file to location specified!!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.genClass = headerClass;
        return this.fieldSpecs;
    }

    private FieldSpec createTotalLenField() {

        FieldSpec lastByte = fieldSpecs.get(fieldSpecs.size() - 1);
        System.out.println(
                "Last byte received = " + lastByte.initializer.toString());

        FieldSpec totalLen = FieldSpec.builder(int.class, "TOTAL_HEADER_LENGTH")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L",
                        Integer.parseInt(lastByte.initializer.toString()) + 1)
                .build();
        return totalLen;
    }

    private void addFields(List<String> fields, List<Integer> startBits,
            int offset) {

        Builder fieldBuilder;
        int index = 0;
        for (String field : fields) {
            // add start bit
            fieldBuilder = FieldSpec
                    .builder(int.class, field.toUpperCase() + "_START_BIT")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC,
                            Modifier.FINAL);
            FieldSpec fieldName = fieldBuilder.initializer("$L", offset)
                    .build();
            fieldSpecs.add(fieldName);

            // add start byte
            fieldBuilder = FieldSpec
                    .builder(int.class, field.toUpperCase() + "_START_BYTE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC,
                            Modifier.FINAL);
            fieldName = fieldBuilder.initializer("$L", offset / 8).build();
            fieldSpecs.add(fieldName);
            offset += startBits.get(index);

            // add end bit
            fieldBuilder = FieldSpec
                    .builder(int.class, field.toUpperCase() + "_END_BIT")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC,
                            Modifier.FINAL);
            fieldName = fieldBuilder.initializer("$L", offset - 1).build();
            fieldSpecs.add(fieldName);

            // add end byte
            fieldBuilder = FieldSpec
                    .builder(int.class, field.toUpperCase() + "_END_BYTE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC,
                            Modifier.FINAL);
            fieldName = fieldBuilder.initializer("$L", (offset - 1) / 8)
                    .build();
            fieldSpecs.add(fieldName);
            index++;
        }
    }
}
