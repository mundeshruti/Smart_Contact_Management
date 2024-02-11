package com.smart.Controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.Dao.ContactRepository;
import com.smart.Dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Path;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonDate(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		User user = userRepository.getUserByUserName(userName);

		System.out.println("USER " + user);

		model.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// add contact
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// process or save the add contact data
	@PostMapping("/process-contact") // same as formaction
	public String processcontact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

//			if(3>2) {
//				throw new Exception();
//			}

			// upload image or file
			if (file.isEmpty()) {

				System.out.println("image empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();
				java.nio.file.Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("image uploaded");

			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("DATA" + contact);

			System.out.println("DATA added");
			// success print
			session.setAttribute("message", new Message("your contact is added !!", "success"));
		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// error msg
			session.setAttribute("message", new Message("something went wrong!!", "danger"));
		}
		return "normal/add_contact_form";
	}

	// show contacts
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "View All Contact");

		// send contact list

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contact";
	}

	// show particular details

	@RequestMapping("/{cId}/contact")
	public String showcontactdetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		System.out.println("CID" + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);

		}

		return "normal/contact_detail";
	}
	// Delete Contact

	@GetMapping("/delete/{cid}")
	public String deletedetails(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
		System.out.println("CID" + cId);
		Contact contact = this.contactRepository.findById(cId).get();

		contact.setUser(null);

		this.contactRepository.delete(contact);

		System.out.println("deleted");

//		session.setAttribute("message", new Message("deleted ","success"));

		return "redirect:/user/show-contact/0";
	}

	// Update Contact
	@PostMapping("/update-contact/{cid}")
	public String UpdateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "update Contact");

		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// Update Contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updatehandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {

		try {
			// old contact detail
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			// image
			if (!file.isEmpty()) {
				// delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				java.nio.file.Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldcontactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);
			// session.setAttribute("message", new Message("your contact is updatd",""));

		} catch (Exception e) {
			e.printStackTrace();

		}
		System.out.println("contact name" + contact.getName());
		System.out.println("contact ID" + contact.getcId());

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// your profile handler
	@GetMapping("/profile")
	public String yourprofile(Model m) {

		m.addAttribute("titile", "profile");

		return "normal/profile";
	}

	// your setting handler
	@GetMapping("/settings")
	public String setting(Model m) {

		m.addAttribute("titile", "Settings");

		return "normal/settings";
	}

	// your change-password
	@PostMapping("/change-password")
	public String changepassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession Session) {
		
		System.out.println("OLD Password "+oldPassword);
		System.out.println("NEW Password "+newPassword);
		

		String userName = principal.getName();
		User CurrentUser = this.userRepository.getUserByUserName(userName);
		
		System.out.println(CurrentUser.getPassword());
		if(this.bCryptPasswordEncoder.matches(oldPassword,CurrentUser.getPassword() )) {
			
			CurrentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(CurrentUser);
			//Session.setAttribute("message", new Message ("your password successfully updated","success"));
		}
		
		else {
			
	    // Session.setAttribute("message", new Message ("your old password incorrect","danger"));
			return "redirect:/user/settings";
		}

		return "redirect:/user/index";
	}

}
