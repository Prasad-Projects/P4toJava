package in.ac.bits.javagen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class BeautyParser {

    @Getter
    private Map<String, String> beautyMap;
    private ArrayList<String> beautyLines;

    @Setter
    private String protocol;

    public Map<String, String> parse(String beautyString)
            throws IllegalArgumentException {
        if (!beautyString.equalsIgnoreCase("NULL")) {
            System.out.println("Beauty string not NULL!! in parser");
            beautyMap = new HashMap<String, String>();
            beautyLines = new ArrayList<String>();

            // pre-processing
            String[] lines = beautyString.split("\\r?\\n");
            removeBlankLines(lines);
            for (int i = 0; i < beautyLines.size(); i++) {
                System.out.println("Line " + i + ":" + beautyLines.get(i));
            }
            int linePtr = 0;
            int protocolPtr = -1;
            while (linePtr < beautyLines.size()) {
                linePtr = hopHeader(linePtr);
                System.out.println("Line pointer now = " + linePtr);
                if (StringUtils.containsIgnoreCase(beautyLines.get(linePtr),
                        protocol)) {
                    System.out.println("The protocol " + protocol
                            + " is on the line: " + linePtr);
                    protocolPtr = linePtr;
                    break;
                } else {
                    throw new IllegalArgumentException(
                            "Invalid beauty string!! Doen't contain the protocol name:"
                                    + protocol);
                }
            }
            protocolPtr++;
            while (!beautyLines.get(protocolPtr).contains("}")) {
                String line = beautyLines.get(protocolPtr);
                String[] tokens = line.split(":");
                System.out.println("Token 0:" + tokens[0].trim());
                System.out.println("Token 1:" + tokens[1].trim());
                beautyMap.put(tokens[0].trim().toLowerCase(), tokens[1].trim().toLowerCase());
                protocolPtr++;
            }

            /*System.out.println("Final values in map as read by parser..");
            for (Entry<String, String> entry : beautyMap.entrySet()) {
                System.out.println(entry.getKey() + "::" + entry.getValue());
            }*/

            return beautyMap;
        } else {
            return null;
        }
    }

    private void removeBlankLines(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() != 0) {
                beautyLines.add(lines[i].trim());
            }
        }
    }

    private int hopHeader(int linePtr) {
        int size = beautyLines.size();
        int ptr = size - 1;
        while (linePtr < size) {
            if (!StringUtils.containsIgnoreCase(beautyLines.get(linePtr),
                    "header")) {
                linePtr++;
            } else {
                ptr = linePtr;
                break;
            }
        }
        return ptr;
    }
}
