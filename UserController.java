package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;


import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	

	@Autowired
	private UserService userService;


	@PostMapping("/register")
	public User registerUser(@RequestParam String username, @RequestParam String password) {
		return userService.registerUser(username, password);
	}

	
	@PostMapping("/login")
	public User authenticateUser(@RequestParam String username, @RequestParam String password) {
		return userService.authenticateUser(username, password);
	}
}