package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Image;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import com.example.demo.service.ImgurService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/images")
public class ImageController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ImgurService imgurService;

	// Upload an image for the user
	@PostMapping("/upload/{username}")
	public String uploadImage(@RequestParam MultipartFile file, @PathVariable String username) {
		// Authenticate user

		User user = userRepository.findByUsername(username);

		userService.authenticateUser(username, user.getPassword());
		return imgurService.uploadImage(file, username);
	}

	@GetMapping("/{userId}")
	public List<Image> getUserImages(@PathVariable Long userId) {
		return imgurService.getImagesByUser(userId);
	}

	@DeleteMapping("/delete")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long userId) {
		try {
			return imgurService.deleteImage(imageId, userId);
		} catch (IOException e) {

			e.printStackTrace();
		}
		return "";
	}
}
