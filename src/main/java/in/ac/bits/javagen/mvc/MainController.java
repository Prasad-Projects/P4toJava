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

@Controller
@RequestMapping("/")
public class MainController {

    @Autowired
    private P4Parser parser;

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

    @RequestMapping(value = "read", method = RequestMethod.POST)
    public @ResponseBody String readFile(@RequestBody Header header,
            HttpServletRequest request) {
        String status = "success";
        System.out.println("Header string: \n" + header.getHeaderString());
        System.out.println("Graph string: \n" + header.getGraphString());
        System.out.println("class = " + header.getProtocol());
        System.out.println("path = " + header.getPath());
        System.out.println("package = " + header.getPackageName());
        System.out.println("Status = " + status);
        parser.generateHeaderClass(header);
        parser.generateAnalyzerClass();
        return status;
    }

}
