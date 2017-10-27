package com.jas.botornot.controllers;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.jas.botornot.models.ActiveUserStore;
import com.jas.botornot.models.ChatMessage;
import com.jas.botornot.models.User;
import com.jas.botornot.services.UserService;
import com.jas.botornot.validator.UserValidator;


@Controller
public class Main {
    private UserService userService;
    // NEW
    private UserValidator userValidator;
    private ChatMessage chatMessage;
    private List<String> names = Arrays.asList("Sheila",
    		"Deana", "Winford", "Janean", "William", "Eugena", "Morris", "Kimber", "Tresa", "Gregorio", "Jacki", "Nakesha", "Catherina", "Timothy", "Carlotta", "Peggie", "Arnoldo", "Nickolas", "Antonetta", "Candyce");

    Random rand = new Random();
    @Autowired
    ActiveUserStore activeUserStore;
    
    // NEW
    public Main(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }
    
    @RequestMapping("/registration")
    public String registerForm(@Valid @ModelAttribute("user") User user) {
        return "redirect:/login";
    }
    
    @PostMapping("/registration")
    public String registration(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        userValidator.validate(user, result);
        if (result.hasErrors()) {
            return "loginPage";
        }
        if(userService.findAdmins().isEmpty()) {
            userService.saveUserWithAdminRole(user);
        }
        else {
        		if(userService.findByUsername(user.getUsername()) == null) {
        			userService.saveWithUserRole(user);
        		}
        		else {
        			model.addAttribute("errorMessage", "Email already in use.");
        			return "loginPage";
        		}
        }
        return "redirect:/dashboard";
    }
    
    @RequestMapping("/login")
    public String login(@RequestParam(value="error", required=false) String error, @RequestParam(value="logout", required=false) String logout, Model model) {
        User user = new User();
        model.addAttribute("user", user);
    		if(error != null) {
            model.addAttribute("errorMessage", "Invalid Credentials, Please try again.");
        }
        if(logout != null) {
        		
            model.addAttribute("logoutMessage", "Logout Successfull!");
        }
        return "loginPage";
    }
    @RequestMapping(value = {"/dashboard", "/home", "/"})
    public String home(Principal principal, Model model, HttpServletResponse response) {
        String username = principal.getName();
        User current = userService.findByUsername(username);
        if(userService.findAdmins().contains(current)) {
        		return "redirect:/admin";
        }
        else {
        		Cookie cookie = new Cookie("ff", "dd");
        		response.addCookie(cookie);
        		model.addAttribute("botName", names.get(rand.nextInt(names.size())));
            model.addAttribute("currentUser", current);
            model.addAttribute("users", activeUserStore.getUsers());
            return "homePage";
        }

    }
    @RequestMapping("/admin")
    public String adminPage(Principal principal, Model model) {
    		List<User> users = userService.findUsers();
    		List<User> admins = userService.findAdmins();
        String username = principal.getName();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", userService.findByUsername(username));
        model.addAttribute("admins", admins);

        return "adminPage";
    }
    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
    		userService.deleteUser(id);
        return "redirect:/admin";
    }
    @RequestMapping("/makeadmin/{id}")
    public String makeAdmin(@PathVariable("id") Long id) {
    		userService.makeAdmin(id);
        return "redirect:/admin";
    }
    @RequestMapping("/chat")
    public String chatPage(Principal principal, Model model, HttpSession session) {
        String username = principal.getName();
        List<User> all = userService.findAll();

        model.addAttribute("all", all);
        model.addAttribute("users", activeUserStore.getUsers());
        model.addAttribute("currentUser", userService.findByUsername(username));
    	return "homePage";
    }
    @RequestMapping(value = "/loggedUsers", method = RequestMethod.GET)
    public String getLoggedUsers(Locale locale, Model model) {
        model.addAttribute("users", activeUserStore.getUsers());
        return "users";
    }
    
    @PostMapping("/pick")
    public String result(@RequestParam("choice") String id, Model model, Principal principal) {
    String username = principal.getName();
    User current = userService.findByUsername(username);
    	System.out.println(id);
    	if(id.equals("0")) {
    		model.addAttribute("result", "You Won");
    		int prev = current.getWonCount();
    		current.setWonCount(prev+1);
    		userService.update(current);
    	}
    	else {
    		model.addAttribute("result", "You Lost");
    		int prev = current.getLossCount();
    		current.setLossCount(prev+1);
    		userService.update(current);
    	}

    	return "result";
    }


}