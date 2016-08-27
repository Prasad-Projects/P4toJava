# P4toJava
This is a support project for BITS-Darshini (https://github.com/prasadtalasila/BITS-Darshini) project.

This web application serves to auto-generate custom analyzer classes and supporting classes when user provides following files in [P4 format](https://github.com/p4lang/papers/blob/master/sosr15/DC.p4/includes/headers.p4) - 
* **Header file** - Contains header field names and their bit width for a protocol
* **Protocol Graph File** - Contains a number of protocol nodes along with their interconnections through switch cases on conditional header fields
* **[Optional] Beauty File** - Contains specific header fields and their desired eventual representation in a format similar to P4. This can be thought of as an extension to p4 language.


Once the application is supplied with these required files for a protocol, it will auto-generate the custom analyzer class, header class and entity class for that protocol. 

If the application is provided with the ["root" directory path](https://github.com/prasadtalasila/PacketAnalyzer/tree/master/src/main/java) for the Packet Analyzer project, and appropriate package name for a custom analyzer(e.g. in.ac.bits.protocolanalyzer.analyzer.link), it will also place the generated class files in appropriate directories. Hence the user of Packet Analyzer will only need to build and deploy the application and the new custom analyzers will be ready to use!!

Auto-generation of Java source code is achieved by utilizing [JavaPoet](https://github.com/square/javapoet) library.
