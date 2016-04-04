package in.ac.bits.javagen;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class P4Parser {

    @Autowired
    private HeaderClassGenerator generator;

    private ArrayList<String> headerLines = new ArrayList<String>();

    private List<String> fieldList;
    private List<Integer> startBits;

    public void generateHeaderClass(String headerString, String className, String pathName, String packageName) {

        fieldList = new ArrayList<String>();
        startBits = new ArrayList<Integer>();

        String[] lines = headerString.split("\\r?\\n");
        removeBlankLines(lines);
        for (int i = 0; i < headerLines.size(); i++) {
            System.out.println("Line: " + i + " " + headerLines.get(i));
        }
        int linePtr = 0;

        if (headerLines.get(linePtr).contains("header")) {
            while (!headerLines.get(linePtr).contains(":")) {
                linePtr++;
            }
            while (!headerLines.get(linePtr).contains("}")) {
                String fieldLine = headerLines.get(linePtr);
                System.out.println("Field line = " + fieldLine);
                String[] tokens = fieldLine.split(":");
                System.out.println("Token 1 = " + tokens[1]);
                String[] subtokens = tokens[1].split(";");
                System.out.println("Start bit = " + subtokens[0]);
                int startBit = Integer.parseInt(subtokens[0].trim());
                String fieldName = tokens[0].trim();
                fieldList.add(fieldName);
                startBits.add(startBit);
                System.out.println("Field name: " + fieldName);
                System.out.println("Field value: " + startBit);
                linePtr++;
            }
            generator.setClassName(className);
            generator.setPackageName(packageName);
            generator.setPath(pathName);
            generator.generateHeaderClass(fieldList, startBits, 0);

        } else {
            System.out
                    .println("Invalid file! Does not contain fields keyword!");
        }

    }

    private int collectNodes(int linePtr) {
        int startLine = linePtr;
        int lines = headerLines.size();
        linePtr++;
        while (linePtr < lines) {
            if (!headerLines.get(linePtr).contains("graph")) {
                linePtr++;
            } else {
                break;
            }
        }

        return linePtr;
    }

    private void removeBlankLines(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() != 0) {
                headerLines.add(lines[i].trim());
            }
        }
    }
}
