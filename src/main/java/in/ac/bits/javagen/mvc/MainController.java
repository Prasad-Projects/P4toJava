package in.ac.bits.javagen.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public @ResponseBody String readFile(
            @RequestParam("graph") String headerString) {
        String status = "success";
        System.out.println("Status = " +status);
        parser.generateHeaderClass(headerString);
        return status;
    }

}
