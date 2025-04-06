package project.kryptotracker.controller;

import project.kryptotracker.service.MysqlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DataController {

    @Autowired private MysqlService mysqlService;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("ct",mysqlService.teste());
        model.addAttribute("msg","Hello World");
        return "home";
    }
}
