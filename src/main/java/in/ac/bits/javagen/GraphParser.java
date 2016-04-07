package in.ac.bits.javagen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import in.ac.bits.javagen.mvc.Input;
import lombok.Getter;
import lombok.Setter;

@Component
public class GraphParser {

    private ArrayList<String> graphLines;

    @Getter
    private String conditionalHeaderField;

    @Getter
    private Map<String, String> valProtocols;

    @Setter
    private Input input;

    public void parse(String graphString) {

        graphLines = new ArrayList<String>();
        valProtocols = new HashMap<String, String>();

        String[] lines = graphString.split("\\r?\\n");
        removeBlankLines(lines);
        for (int i = 0; i < graphLines.size(); i++) {
            System.out.println("Line: " + i + " " + graphLines.get(i));
        }
        int linePtr = 0;
        int protocolPtr = -1;
        while (linePtr < graphLines.size()) {
            linePtr = hopGraph(linePtr);
            System.out.println("Line pointer now = " + linePtr);
            if (StringUtils.containsIgnoreCase(graphLines.get(linePtr),
                    input.getProtocol())) {
                System.out.println("The protocol " + input.getProtocol()
                        + " is on the line: " + linePtr);
                protocolPtr = linePtr;
                break;
            } else {
                linePtr++;
            }
        }
        protocolPtr = hopswitch(protocolPtr);
        if (protocolPtr > 0) {
            setCondition(protocolPtr);
            System.out.println("Conditional header =" + conditionalHeaderField);
            protocolPtr++;
            while (StringUtils.containsIgnoreCase(graphLines.get(protocolPtr),
                    "case")) {
                addCases(protocolPtr);
                protocolPtr++;
            }
        } else {
            conditionalHeaderField = "NULL";
            System.out.println("No switch statement!!");
        }

    }

    private void addCases(int protocolPtr) {
        String[] tokens = graphLines.get(protocolPtr).split(":");
        System.out.println("tokens[0] = " + tokens[0]);
        String[] caseVal = tokens[0].trim().split("\\s");
        for (int i = 0; i < caseVal.length; i++) {
            System.out.println("Caseval " + i + "=" + caseVal[i]);
        }
        String protocol = tokens[1].trim();
        protocol = protocol.substring(0, protocol.length() - 1);
        System.out.println("Adding to valprotocol: {" + caseVal[1].trim() + "="
                + protocol + "}");
        valProtocols.put(caseVal[1].trim(), protocol);
    }

    private int hopswitch(int protocolPtr) {
        if (protocolPtr < 0) {
            return protocolPtr;
        } else {
            int size = graphLines.size();
            int lineptr = -1;
            while (protocolPtr < size
                    && !graphLines.get(protocolPtr).contains("}")) {
                if (StringUtils.containsIgnoreCase(graphLines.get(protocolPtr),
                        "switch")) {
                    lineptr = protocolPtr;
                    break;
                } else {
                    protocolPtr++;
                }
            }
            return lineptr;
        }
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
        int ptr = size - 1;
        while (linePtr < size) {
            if (!StringUtils.containsIgnoreCase(graphLines.get(linePtr),
                    "graph")) {
                linePtr++;
            } else {
                ptr = linePtr;
                break;
            }
        }
        return ptr;
    }

    private void removeBlankLines(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() != 0) {
                graphLines.add(lines[i]);
            }
        }
    }

}
