package in.ac.bits.javagen;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.squareup.javapoet.FieldSpec;

import in.ac.bits.javagen.mvc.Header;

@Component
public class P4Parser {

    @Autowired
    private HeaderClassGenerator headerGenerator;

    @Autowired
    private AnalyzerGenerator analyzerGenerator;

    private ArrayList<String> headerLines;
    private List<String> fieldList;
    private List<Integer> startBits;

    private List<FieldSpec> generatedFields;

    private Header header;

    private void setGeneratedFields(List<FieldSpec> fields) {
        generatedFields = fields;
    }

    public void generateHeaderClass(Header header) {

        this.header = header;
        String headerString = header.getHeaderString();
        String className = header.getProtocol() + "Header";
        String path = header.getPath();
        String packageName = header.getPackageName();

        fieldList = new ArrayList<String>();
        startBits = new ArrayList<Integer>();
        headerLines = new ArrayList<String>();

        // pre-processing
        String[] lines = headerString.split("\\r?\\n");
        removeBlankLines(lines);
        int linePtr = 0;
        linePtr = getToFields(linePtr);

        while (!headerLines.get(linePtr).contains("}")
                && linePtr < headerLines.size()) {
            String fieldLine = headerLines.get(linePtr);
            String[] tokens = fieldLine.split(":");
            String[] subtokens = tokens[1].split(";");
            int startBit = Integer.parseInt(subtokens[0].trim());
            String fieldName = tokens[0].trim();
            fieldList.add(fieldName);
            startBits.add(startBit);
            linePtr++;
        }
        headerGenerator.setClassName(className);
        headerGenerator.setPackageName(packageName);
        headerGenerator.setPath(path);
        List<FieldSpec> fields = headerGenerator.generateHeaderClass(fieldList,
                startBits, 0);

        /*
         * save the list of generated fields to be used for generating analyzer
         * class
         */
        setGeneratedFields(fields);

    }

    private int getToFields(int linePtr) {
        if (headerLines.get(linePtr).contains("header")) {
            while (!headerLines.get(linePtr).contains(":")) {
                linePtr++;
            }
            return linePtr;
        } else {
            throw new RuntimeException(
                    "File doesn't contain the keyword: fields");
        }
    }

    private void removeBlankLines(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() != 0) {
                headerLines.add(lines[i].trim());
            }
        }
    }

    public void generateAnalyzerClass() {
        analyzerGenerator.setHeaderVars(generatedFields);
        analyzerGenerator.setHeaderFields(fieldList);
        analyzerGenerator.setProtocol(header.getProtocol());
        analyzerGenerator.setHeaderClass(headerGenerator.getHeaderClass());
        analyzerGenerator.setHeader(header);
        analyzerGenerator.generateAnalyzer();
    }
}
