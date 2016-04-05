package in.ac.bits.javagen.mvc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Header {

    private String headerString;
    private String protocol;
    private String path;
    private String packageName;
}
