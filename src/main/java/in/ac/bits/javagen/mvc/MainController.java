package in.ac.bits.javagen.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import in.ac.bits.javagen.P4Parser;
import in.ac.bits.javagen.ProtocolCheckerGenerator;

@Controller
@RequestMapping("/")
public class MainController {

    @Autowired
    private P4Parser parser;

    @Autowired
    private ProtocolCheckerGenerator generator;

    @RequestMapping
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("home");
        return mav;
    }

    @RequestMapping("/index")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");
        return mav;
    }
    
    @RequestMapping("/clear")
    public @ResponseBody String clear() {
        parser.clearAll();
        return "success";
    }

    @RequestMapping("/checker")
    public @ResponseBody String checker(@RequestBody Input input,
            HttpServletRequest request) {
        generator.setInput(input);
        generator.generateChecker();
        return "success";
    }

    @RequestMapping(value = "read", method = RequestMethod.POST)
    public @ResponseBody String readFile(@RequestBody Input input,
            HttpServletRequest request) {
        String status = "success";
        System.out.println("Header string: \n" + input.getHeaderString());
        System.out.println("class = " + input.getProtocol());
        System.out.println("path = " + input.getPath());
        System.out.println("package = " + input.getPackageName());
        System.out.println("Status = " + status);
        parser.generateHeaderClass(input);
        parser.generateAnalyzerClass();
        return status;
    }

}
