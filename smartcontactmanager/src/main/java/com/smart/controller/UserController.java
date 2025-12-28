package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private ContactRepository contactRepository;

	private final UserRepository userRepository;

	UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME" + userName);

		// get the user Using username email
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);
		model.addAttribute("user", user);

	}

	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
//    	String userName = principal.getName();
//        User user = userRepository.getUserByUserName(userName);
//        
//        if (user == null) {
//            // Handle case where user isn't found
//            return "redirect:/login";
//        }
//        
//        model.addAttribute("user", user);
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";

	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file..
			if (file.isEmpty()) {
				// if the file is empty the try our message

				System.out.println("File is empty");
				contact.setImage("contact.png");
			} else {
				// file the file to folder and update the name to contact
				// File handling
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			// Set contact's user
			contact.setUser(user);
			user.getContact().add(contact);
			this.userRepository.save(user);
			System.out.println("Added to database");

			// message success
			session.setAttribute("message", new Message("Your Contact is add!! Addmore ", "success"));

			System.out.println("Data" + contact);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR" + e.getMessage());
			// message error
			session.setAttribute("message", new Message("Your Contact is add!! Addmore ", "success"));
		}
		// Remove message after displaying
		// session.removeAttribute("message");
		return "normal/add_contact_form";

	}
	// show contacts handler
	// per page=5[n]
	// current page=0[current (page)]

	@GetMapping("show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		m.addAttribute("title", "Shows User Contacts");

		// contact ko list pathaunu parxa tyo database bat aauxa

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// current page-page
		// contact perpage-5
		Pageable pageable = PageRequest.of(page, 5);

		// List<Contact> contacts =
		// this.contactRepository.findContactsByUser(user.getId());

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalpages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// showing particular contact details
	@GetMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		if (contactOptional.isEmpty()) {
			model.addAttribute("contact", null);
			return "normal/contact_details";
		}

		Contact contact = contactOptional.get();
		User user = this.userRepository.getUserByUserName(principal.getName());

		if (user.getId() != contact.getUser().getId()) {
			model.addAttribute("contact", null);
			return "normal/contact_details";
		}

		model.addAttribute("contact", contact);
		return "normal/contact_details";
	}

//	@GetMapping("/{cid}/contact")
//	public String showContactDetail(@PathVariable("cid") Integer cId, Model model, Principal principal) {
//
//		System.out.println("CID" + cId);
//		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
//
//		Contact contact = contactOptional.get();
//		//
//		String userName = principal.getName();
//		User user = this.userRepository.getUserByUserName(userName);
//
//		if (user.getId() == contact.getUser().getId()) {
//			model.addAttribute("contact", contact);
//			model.addAttribute("title", contact.getName());
//		}
//
//		return "normal/contact_details";
//
//	}
//	//delete contact handler
//	@GetMapping("/delete/{cid}")
//	public String deleteContact(@PathVariable("cid") Integer cId , Model model, HttpSession session) {
//		
//		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
//		Contact contact = contactOptional.get();
//		
//		//check..
//		System.out.println("contact" +contact.getCid());
//		contact.setUser(null);
//		//remove image of the contact
//		
//		this.contactRepository.delete(contact);
//		
//		session.setAttribute("Message", new Message("contact deleted successfully", "success"));
//		
//		
//		
//		
//		return "redirect:/user/show-contacts/0";

	// delete contact handler
//	@GetMapping("/delete/{cid}")
//	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal,
//			HttpServletRequest request) {
//
//		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
//
//		if (contactOptional.isPresent()) {
//			Contact contact = contactOptional.get();
//
//			// Get current user by username (email)
//			String username = principal.getName();
//			User currentUser = this.userRepository.getUserByUserName(username);
//
//			// Check if the contact belongs to the logged-in user
//			if (contact.getUser().getId() == currentUser.getId()) {
//
//				// Remove contact image if not default
//				String imageName = contact.getImage();
//				if (imageName != null && !"default.png".equals(imageName)) {
//					String uploadDir = request.getServletContext().getRealPath("/img/");
//					File imageFile = new File(uploadDir + File.separator + imageName);
//					if (imageFile.exists()) {
//						imageFile.delete();
//					}
//				}
//
//				// Disassociate and delete
//				contact.setUser(null);
//				this.contactRepository.delete(contact);
//
//				request.getSession().setAttribute("Message", new Message("Contact deleted successfully!", "success"));
//			} else {
//				request.getSession().setAttribute("Message",
//						new Message("You are not authorized to delete this contact!", "danger"));
//			}
//		} else {
//			request.getSession().setAttribute("Message", new Message("Contact not found!", "danger"));
//		}
//
//		return "redirect:/user/show-contacts/0";
//	}
	// delete contact handler Improve version which delete data from database also

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal,
	                            HttpServletRequest request) {

	    Optional<Contact> contactOptional = this.contactRepository.findById(cId);

	    if (contactOptional.isPresent()) {
	        Contact contact = contactOptional.get();

	        String username = principal.getName();
	        User currentUser = this.userRepository.getUserByUserName(username);

	        if (contact.getUser().getId() == currentUser.getId()) {

	            // Delete contact image
	            String imageName = contact.getImage();
	            if (imageName != null && !"default.png".equals(imageName)) {
	                String uploadDir = request.getServletContext().getRealPath("/img/");
	                File imageFile = new File(uploadDir + File.separator + imageName);
	                if (imageFile.exists()) {
	                    imageFile.delete();
	                }
	            }

	            // Directly delete the contact
	            this.contactRepository.delete(contact);

	            request.getSession().setAttribute("Message", new Message("Contact deleted successfully!", "success"));
	        } else {
	            request.getSession().setAttribute("Message", new Message("You are not authorized to delete this contact!", "danger"));
	        }
	    } else {
	        request.getSession().setAttribute("Message", new Message("Contact not found!", "danger"));
	    }

	    return "redirect:/user/show-contacts/0";
	}



	// open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId, Model m) {
		m.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";

	}

	// update contact handler

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpServletRequest request, Principal principal) {
		try {
			//old contact details 
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty()) {
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			request.getSession().setAttribute("Message", new Message("Your contact is updated successfully!", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT NAME" +contact.getName());
		System.out.println("CONTACT ID" +contact.getCid());
		

		return "redirect:/user/" + contact.getCid()+"/contact";

	}
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		String username = principal.getName(); // Get logged-in username
	    User user = this.userRepository.getUserByUserName(username); // Get user from DB
	    model.addAttribute("title","Profile page"); // Add user to model
		return "normal/profile";
		
	}

}
