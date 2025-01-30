package com.example.demo.service;

import java.io.IOException;
//import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Image;
import com.example.demo.model.User;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ImgurService {

	@Value("${imgur.client-id}")
	private String clientId;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private UserRepository userRepository;

	private static final String IMGUR_API_URL = "https://api.imgur.com/3/upload";
	private static final String IMGUR_API_URL_DELETE = "https://api.imgur.com/3/image";

	private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);
	OkHttpClient client = new OkHttpClient();

	public String uploadImage(MultipartFile file, String userName) {
		try {

			//InputStream inputStream = file.getInputStream();
			logger.info("ImgurService : uploadImage "+userName);
			MediaType mediaType = MediaType.parse("image/*");
			RequestBody fileBody = RequestBody.create(mediaType, file.getBytes());

			MultipartBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("image", file.getOriginalFilename(), fileBody).build();

			Request request = new Request.Builder().url(IMGUR_API_URL)
					.addHeader("Authorization", "Client-ID " + clientId).post(requestBody).build();

			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected code " + response);
			}

			String responseBody = response.body().string();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonResponse = objectMapper.readTree(responseBody);

			String imageUrl = jsonResponse.path("data").path("link").asText();
			String deleteHash = jsonResponse.path("data").path("deletehash").asText();

			Image image = new Image();
			image.setUrl(imageUrl);
			image.setDeleteHash(deleteHash);

			User userOptional = userRepository.findByUsername(userName);

			if (!userOptional.equals(null)) {

				image.setUser(userOptional);
				imageRepository.save(image);

			}
			return jsonResponse.path("data").path("link").asText();
		} catch (IOException e) {

			throw new RuntimeException("Failed to upload image", e);
		}
	}

	public List<Image> getImagesByUser(Long userId) {
		logger.info("ImgurService : getImagesByUser "+userId);
		return imageRepository.findByUserId(userId);
	}

	public String deleteImage(Long imageId, Long userId) throws IOException {
		logger.info("ImgurService : deleteImage "+userId+" "+imageId);
		Optional<Image> imageOptional = imageRepository.findById(imageId);
		if (imageOptional.isEmpty()) {
			throw new RuntimeException("Image not found");
		}

		Image image = imageOptional.get();
		if (!image.getUser().getId().equals(userId)) {
			throw new RuntimeException("Unauthorized to delete this image");
		}

		Request request = new Request.Builder().url(IMGUR_API_URL_DELETE + "/" + image.getDeleteHash()).delete()
				.addHeader("Authorization", "Client-ID " + clientId).build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Imgur API delete failed: " + response.message());
		}

		imageRepository.delete(image);
		return "Image deleted successfully";
	}

}
