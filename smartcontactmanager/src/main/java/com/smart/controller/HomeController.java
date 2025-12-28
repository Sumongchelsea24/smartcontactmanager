package com.smart.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.SmartcontactmanagerApplication;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	
	private BCryptPasswordEncoder passwordEncoder;
	
	private final SmartcontactmanagerApplication smartcontactmanagerApplication;

	@Autowired
	private UserRepository userRepository;

	HomeController(SmartcontactmanagerApplication smartcontactmanagerApplication) {
		this.smartcontactmanagerApplication = smartcontactmanagerApplication;
	}

//	@GetMapping("/work")
//	@ResponseBody
//	public String test() {
//		User user = new User();
//		
//		user.setName("Sujan Shrestha");
//		user.setEmail("Sujan123@gmail.com");
//		userRepository.save(user);
//		return "Working";
//		
//	}
	@GetMapping("/")
    public String home() {
        return "home"; // This should match a `home.html` file in `src/main/resources/templates/`
    }
	

	@GetMapping("/home")
	public String home(Model m) {

		m.addAttribute("title", "Home- Smart Contact Manager");

		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {

		m.addAttribute("title", "Home- Smart Contact Manager");

		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model m) {

		m.addAttribute("title", "SignUp- Smart Contact Manager");
		m.addAttribute("user", new User());

		// ✅ Ensure no old message lingers
		m.addAttribute("message", null);

		return "signup";
	}
	@GetMapping("/login")
	public String login(Model model) {
	    model.addAttribute("title", "Login - Smart Contact Manager");
	    return "login"; // It should resolve to `login.html` inside `templates/`
	}

	// handler for registering user

//	@PostMapping("/do_register")
//	public String registerUser(@ModelAttribute("user") User user,
//			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
//		
//		try {
//			if(!agreement) {
//				System.out.println("You have not agreed the terms and condition");
//			    throw new Exception("You have not agreed the terms and condition");
//			}
//			
//			user.setRole("ROLE_USER");
//			user.setEnabled(true);
//			user.setImageUrl(null);
//			user.setPassword(passwordEncoder.encode(user.getPassword()));
//			
//			
//			
//			System.out.println("Agreement"+agreement);
//			System.out.println("USER" +user);
//			
//			User result = this.userRepository.save(user);
//			model.addAttribute("user", new User());
//			//session.setAttribute("message", new Message("Successfully Register!!!", "alert-success"));
//			
//			
//			// ✅ Store message in Model (not session)
//	        model.addAttribute("message", new Message("Successfully Registered!", "alert-success"));
//			
//			return "signup";
//		} catch (Exception e) {
//			e.printStackTrace();
//			model.addAttribute("user", user);
//			//session.setAttribute("message", new Message("Something went wrong!!!", "alert-danger"));
//			
//			// ✅ Store error message in Model (not session)
//	        model.addAttribute("message", new Message("Something went wrong!", "alert-danger"));
//		}
//		
//
//		return "signup";
//
//	}
	
//	@PostMapping("/do_register")
//	public String registerUser(@Valid@ModelAttribute("user") User user,BindingResult result1,
//			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model) {
//
//		System.out.println("Agreement checkbox value: " + agreement); // Debugging output
//
//		try {
//			if (!agreement) {
//				throw new Exception("You have not agreed to the terms and conditions.");
//			}
//			
//			if(result1.hasErrors()) {
//				System.out.println("ERROR" +result1.toString());
//				model.addAttribute("user", user);
//				
//				return "signup";
//			}
//
//			user.setRole("ROLE_USER");
//			user.setEnabled(true);
//			user.setImageUrl(null);
//			
//			user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//			this.userRepository.save(user);
//			model.addAttribute("user", new User());
//			model.addAttribute("message", new Message("Successfully Registered!", "alert-success"));
//
//			return "signup";
//		} catch (Exception e) {
//			e.printStackTrace();
//			model.addAttribute("user", user);
//			 model.addAttribute("message", new Message(e.getMessage(), "alert-danger"));
//			return "signup";
//		}
//	}
	
	@PostMapping("/do_register")
	public String registerUser(
	        @Valid @ModelAttribute("user") User user,
	        BindingResult result1,
	        Model model) {

	    try {
	        // Check agreement
	        if (!user.isAgreement()) {
	            throw new Exception("You must agree to the terms and conditions");
	        }

	        // Check if email exists
	        if (userRepository.existsByEmail(user.getEmail())) {
	            throw new Exception("User with this email is already registered!");
	        }

	        if (result1.hasErrors()) {
	            model.addAttribute("user", user);
	            return "signup";
	        }

	        // Set user properties
	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setImageUrl("default.png");
	        user.setPassword(passwordEncoder.encode(user.getPassword()));

	        userRepository.save(user);
	        
	        model.addAttribute("user", new User());
	        model.addAttribute("message", 
	            new Message("Registration successful!", "alert-success"));
	        
	        return "signup";

	    } catch (Exception e) {
	        model.addAttribute("user", user);
	        model.addAttribute("message", 
	            new Message(e.getMessage(), "alert-danger"));
	        return "signup";
	    }
	}

}
