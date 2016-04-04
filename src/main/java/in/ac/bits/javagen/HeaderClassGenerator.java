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

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void generateHeaderClass(List<String> fields,
            List<Integer> startBits, int offset) {

        fieldSpecs = new ArrayList<FieldSpec>();
        addFields(fields, startBits, offset);

        com.squareup.javapoet.TypeSpec.Builder builder = TypeSpec
                .classBuilder(this.className);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.addFields(fieldSpecs);
        TypeSpec headerClass = builder.build();

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
            offset += startBits.get(index);

            // add end bit
            fieldBuilder = FieldSpec
                    .builder(int.class, field.toUpperCase() + "_END_BIT")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC,
                            Modifier.FINAL);
            fieldName = fieldBuilder.initializer("$L", offset-1).build();
            fieldSpecs.add(fieldName);
            index++;
        }
    }
}
