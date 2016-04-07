package in.ac.bits.javagen;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import in.ac.bits.javagen.mvc.Header;
import lombok.Getter;
import lombok.Setter;

@Component
public class GraphParser {

    private ArrayList<String> graphLines;

    @Getter
    private String conditionalHeaderField;

    @Setter
    private Header header;

    public void parse(String graphString) {

        graphLines = new ArrayList<String>();

        String[] lines = graphString.split("\\r?\\n");
        removeBlankLines(lines);
        for (int i = 0; i < graphLines.size(); i++) {
            System.out.println("Line: " + i + " " + graphLines.get(i));
        }
        int linePtr = 0;
        int protocolPtr = -1;
        while (linePtr < graphLines.size()) {
            linePtr = hopGraph(linePtr);
            if (StringUtils.containsIgnoreCase(graphLines.get(linePtr),
                    header.getProtocol())) {
                System.out.println("The protocol " + header.getProtocol()
                        + " is on the line: " + linePtr);
                protocolPtr = linePtr;
                break;
            } else {
                linePtr++;
            }
        }
        if (protocolPtr > 0) {
            protocolPtr = hopswitch(protocolPtr);
            setCondition(protocolPtr);
            System.out.println("Conditional header =" + conditionalHeaderField);
        }

    }

    private int hopswitch(int protocolPtr) {
        int size = graphLines.size();
        while (protocolPtr < size
                && !graphLines.get(protocolPtr).contains("}")) {
            if (StringUtils.containsIgnoreCase(graphLines.get(protocolPtr),
                    "switch")) {
                break;
            } else {
                protocolPtr++;
            }
        }
        return protocolPtr;
    }

    private void setCondition(int linePtr) {
        String switchLine = graphLines.get(linePtr);
        System.out.println("Switchline: " + switchLine);
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(switchLine);
        while (m.find()) {
            conditionalHeaderField = m.group(1).trim();
        }
    }

    private int hopGraph(int linePtr) {
        int size = graphLines.size();
        while (linePtr < size) {
            if (!StringUtils.containsIgnoreCase(graphLines.get(linePtr),
                    "graph")) {
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
                graphLines.add(lines[i]);
            }
        }
    }

}
