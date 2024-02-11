package com.smart.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.Dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home-Smart contact mmanager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
    	model.addAttribute("title","About-Smart contact mmanager");
		return "about";
	}
	

	@RequestMapping("/signup")
	public String signup(Model model) {
    	model.addAttribute("title","Register-Smart contact mmanager");
    	model.addAttribute("user", new User());
		return "signup";
	}
// handler 
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user")User user,BindingResult result1,
			@RequestParam(value="agrement", defaultValue = "false")
	 boolean agrement, Model model, HttpSession session) 
	{
		try {
			if(!agrement) {
				System.out.println("you have not agree the terms and condition");
				throw new Exception("you have not agree the terms and condition");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR" +result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
	
			System.out.println("Agrement" +agrement);
			System.out.println("User" +user);
			
			User result= this.userRepository.save(user);		
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("SUCESSFULLY REGISTER","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong" + e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	// login
	@GetMapping("/signin")
	public String Signin(Model model) {
		return "login";
	}
}
