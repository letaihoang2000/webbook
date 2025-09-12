package com.example.webbook.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadImage(MultipartFile file, UUID imageId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG, GIF, WEBP are allowed");
        }

        try {
            // Method 2: Using separate folder parameter (Cloudinary recommended)
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "webbook/user_avatars",           // Destination folder
                    "public_id", imageId.toString(),            // Just the filename/ID
                    "resource_type", "image",
                    "transformation", new Transformation()
                            .width(300).height(300)
                            .crop("fill")
                            .quality("auto")
                            .fetchFormat("auto") // Automatic format optimization
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            // Debug logging
            System.out.println("Public ID: " + uploadResult.get("public_id"));
            System.out.println("URL: " + uploadResult.get("secure_url"));

            // Return the secure URL
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    private boolean hasValidExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    // Optional: Method to delete image from Cloudinary
    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.contains("cloudinary.com")) {
                // Extract public ID from URL
                String publicId = extractPublicIdFromUrl(imageUrl);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
            }
        } catch (IOException e) {
            // Log error but don't throw - deletion failure shouldn't break user operations
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String url) {
        // Extract public ID from Cloudinary URL
        // Example: https://res.cloudinary.com/demo/image/upload/v1234567890/webbook/user_avatars/uuid.jpg
        // The public_id for Method 2 is: "webbook/user_avatars/uuid"
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String pathWithVersion = parts[1];
                // Remove version number (v1234567890/)
                String pathWithoutVersion = pathWithVersion.replaceFirst("v\\d+/", "");
                // Remove file extension
                int lastDotIndex = pathWithoutVersion.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    return pathWithoutVersion.substring(0, lastDotIndex);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting public ID from URL: " + url + " - " + e.getMessage());
        }
        return null;
    }
}
