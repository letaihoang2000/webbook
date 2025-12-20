package com.example.webbook.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.folder.author}")
    private String authorFolder;

    @Value("${supabase.storage.folder.book-image}")
    private String bookImageFolder;

    @Value("${supabase.storage.folder.book-content}")
    private String bookContentFolder;

    private final OkHttpClient client;

    public SupabaseStorageService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Upload book cover image to Supabase Storage using book title as filename
     *
     * @param file      The image file to upload
     * @param bookTitle The title of the book (will be used as filename)
     * @return Public URL of the uploaded image
     */
    public String uploadBookImage(MultipartFile file, String bookTitle) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (max 10MB)
        validateFileSize(file, 10 * 1024 * 1024);

        // Get file extension from uploaded file
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // If no extension, default to .jpg
        if (extension.isEmpty()) {
            extension = ".jpg";
        }

        // Sanitize book title for filename
        String sanitizedTitle = sanitizeFilename(bookTitle);
        String filename = sanitizedTitle + extension;

        // Full path: book_image/BookTitle.jpg
        String fullPath = bookImageFolder + "/" + filename;

        // Upload to Supabase
        uploadToSupabase(fullPath, file.getBytes(), contentType);

        // Return public URL
        return getPublicUrl(fullPath);
    }

    /**
     * Upload book content (PDF) to Supabase Storage using book title as filename
     *
     * @param file      The PDF file to upload
     * @param bookTitle The title of the book (will be used as filename)
     * @return Public URL of the uploaded PDF
     */
    public String uploadBookContent(MultipartFile file, String bookTitle) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        // Validate file size (max 50MB)
        validateFileSize(file, 50 * 1024 * 1024);

        // Sanitize book title for filename
        String sanitizedTitle = sanitizeFilename(bookTitle);
        String filename = sanitizedTitle + ".pdf";

        // Full path: book_content/BookTitle.pdf
        String fullPath = bookContentFolder + "/" + filename;

        // Upload to Supabase
        uploadToSupabase(fullPath, file.getBytes(), contentType);

        // Return public URL
        return getPublicUrl(fullPath);
    }

    /**
     * Upload author image to Supabase Storage using author name as filename
     *
     * @param file       The image file to upload
     * @param authorName The name of the author (will be used as filename)
     * @return Public URL of the uploaded image
     */
    public String uploadAuthorImage(MultipartFile file, String authorName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (max 5MB)
        validateFileSize(file, 5 * 1024 * 1024);

        // Get file extension from uploaded file
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // If no extension, default to .jpg
        if (extension.isEmpty()) {
            extension = ".jpg";
        }

        // Sanitize author name for filename
        String sanitizedName = sanitizeFilename(authorName);
        String filename = sanitizedName + extension;

        // Full path: author/AuthorName.jpg
        String fullPath = authorFolder + "/" + filename;

        // Upload to Supabase
        uploadToSupabase(fullPath, file.getBytes(), contentType);

        // Return public URL
        return getPublicUrl(fullPath);
    }

    /**
     * Upload author image from byte array (for renaming operations)
     */
    public String uploadAuthorImageFromBytes(byte[] fileData, String authorName, String extension, String contentType) throws IOException {
        if (fileData == null || fileData.length == 0) {
            return null;
        }

        String sanitizedName = sanitizeFilename(authorName);
        String filename = sanitizedName + extension;
        String fullPath = authorFolder + "/" + filename;

        uploadToSupabase(fullPath, fileData, contentType);
        return getPublicUrl(fullPath);
    }

    /**
     * Sanitize filename - remove or replace special characters
     * Keeps letters, numbers, spaces, hyphens, and apostrophes
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "untitled";
        }

        // Remove leading/trailing whitespace
        filename = filename.trim();

        // Replace multiple spaces with single space
        filename = filename.replaceAll("\\s+", " ");

        // Keep only safe characters: letters, numbers, spaces, hyphens, apostrophes, periods
        // This matches your sample data which has apostrophes and spaces
        filename = filename.replaceAll("[^a-zA-Z0-9 '.-]", "");

        // If filename is empty after sanitization, use default
        if (filename.isEmpty()) {
            return "untitled";
        }

        return filename;
    }

    /**
     * Upload file to Supabase Storage using REST API
     * @param fileData File bytes
     * @param contentType MIME type
     */
    private void uploadToSupabase(String filePath, byte[] fileData, String contentType) throws IOException {
        // URL encode the file path to handle spaces and special characters
        String encodedPath = encodeFilePath(filePath);

        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodedPath;

        System.out.println("=== UPLOAD FILE OPERATION ===");
        System.out.println("Uploading to: " + url);
        System.out.println("File path: " + filePath);
        System.out.println("Content type: " + contentType);
        System.out.println("File size: " + fileData.length + " bytes");

        RequestBody body = RequestBody.create(fileData, MediaType.parse(contentType));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", contentType)
                .addHeader("apikey", supabaseKey)
                .addHeader("x-upsert", "true")  // This should overwrite existing files
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";

            if (!response.isSuccessful()) {
                System.err.println("Upload failed!");
                System.err.println("Response code: " + response.code());
                System.err.println("Response body: " + responseBody);
                throw new IOException("Failed to upload file to Supabase: " + response.code() + " - " + responseBody);
            }

            System.out.println("✓ Upload successful!");
            System.out.println("Response: " + responseBody);
        }
        System.out.println("=== END UPLOAD OPERATION ===\n");
    }


    /**
     * Delete a file from Supabase Storage
     * @param fileUrl The public URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            System.out.println("Delete skipped: fileUrl is null or empty");
            return;
        }

        try {
            System.out.println("=== DELETE FILE OPERATION ===");
            System.out.println("File URL to delete: " + fileUrl);

            String filePath = extractFilePathFromUrl(fileUrl);
            System.out.println("Extracted file path: " + filePath);

            String encodedPath = encodeFilePath(filePath);
            System.out.println("Encoded file path: " + encodedPath);

            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodedPath;
            System.out.println("Delete URL: " + deleteUrl);

            Request request = new Request.Builder()
                    .url(deleteUrl)
                    .delete()
                    .addHeader("Authorization", "Bearer " + supabaseKey)
                    .addHeader("apikey", supabaseKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";

                if (!response.isSuccessful()) {
                    System.err.println("Failed to delete file from Supabase");
                    System.err.println("Response code: " + response.code());
                    System.err.println("Response body: " + responseBody);
                } else {
                    System.out.println("✓ File deleted successfully: " + filePath);
                    System.out.println("Response: " + responseBody);
                }
            }
            System.out.println("=== END DELETE OPERATION ===\n");

        } catch (Exception e) {
            System.err.println("Exception during file deletion: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * URL encode file path to handle spaces and special characters
     */
    private String encodeFilePath(String filePath) throws UnsupportedEncodingException {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // Split by "/" to encode each part separately
        String[] parts = filePath.split("/");
        StringBuilder encoded = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                encoded.append("/");
            }
            // URL encode each part (handles spaces, apostrophes, etc.)
            encoded.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
        }

        return encoded.toString();
    }

    /**
     * Get public URL for a file in Supabase Storage
     */
    private String getPublicUrl(String filePath) throws UnsupportedEncodingException {
        // URL encode the path for the public URL
        String encodedPath = encodeFilePath(filePath);

        // URL format: https://jvrpympmziwwpedpoagx.supabase.co/storage/v1/object/public/book_content/book_image/Book%20Title.jpg
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + encodedPath;
    }

    /**
     * Extract file path from Supabase URL
     * Returns: book_image/Book Title.jpg (decoded)
     */
    private String extractFilePathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        // Find the bucket name in the URL and extract everything after it
        String searchPattern = "/public/" + bucket + "/";
        int index = url.indexOf(searchPattern);

        if (index != -1) {
            String encodedPath = url.substring(index + searchPattern.length());
            // Decode URL encoding
            return java.net.URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
        }

        // Fallback: just get the last two segments (folder/filename)
        String[] parts = url.split("/");
        if (parts.length >= 2) {
            String folder = parts[parts.length - 2];
            String filename = parts[parts.length - 1];
            folder = java.net.URLDecoder.decode(folder, StandardCharsets.UTF_8);
            filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);
            return folder + "/" + filename;
        }

        return "";
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Validate file size
     */
    public void validateFileSize(MultipartFile file, long maxSizeInBytes) {
        if (file.getSize() > maxSizeInBytes) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (maxSizeInBytes / 1024 / 1024) + "MB"
            );
        }
    }

    /**
     * Get file size in MB
     */
    public double getFileSizeInMB(MultipartFile file) {
        return (double) file.getSize() / (1024 * 1024);
    }

    /**
     * Download a file from Supabase Storage
     *
     * @param fileUrl The public URL of the file
     * @return File bytes
     */
    public byte[] downloadFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        Request request = new Request.Builder()
                .url(fileUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response.code());
            }

            return response.body() != null ? response.body().bytes() : null;
        }
    }

    /**
     * Upload book image from byte array (for renaming operations)
     *
     * @param fileData    The image bytes
     * @param bookTitle   The title of the book (will be used as filename)
     * @param extension   File extension (e.g., ".jpg")
     * @param contentType MIME type
     * @return Public URL of the uploaded image
     */
    public String uploadBookImageFromBytes(byte[] fileData, String bookTitle, String extension, String contentType) throws IOException {
        if (fileData == null || fileData.length == 0) {
            return null;
        }

        // Sanitize book title for filename
        String sanitizedTitle = sanitizeFilename(bookTitle);
        String filename = sanitizedTitle + extension;

        // Full path: book_image/BookTitle.jpg
        String fullPath = bookImageFolder + "/" + filename;

        // Upload to Supabase
        uploadToSupabase(fullPath, fileData, contentType);

        // Return public URL
        return getPublicUrl(fullPath);
    }

    /**
     * Upload book content from byte array (for renaming operations)
     *
     * @param fileData  The PDF bytes
     * @param bookTitle The title of the book (will be used as filename)
     * @return Public URL of the uploaded PDF
     */
    public String uploadBookContentFromBytes(byte[] fileData, String bookTitle) throws IOException {
        if (fileData == null || fileData.length == 0) {
            return null;
        }

        // Sanitize book title for filename
        String sanitizedTitle = sanitizeFilename(bookTitle);
        String filename = sanitizedTitle + ".pdf";

        // Full path: book_content/BookTitle.pdf
        String fullPath = bookContentFolder + "/" + filename;

        // Upload to Supabase
        uploadToSupabase(fullPath, fileData, "application/pdf");

        // Return public URL
        return getPublicUrl(fullPath);
    }

    /**
     * Public method to access sanitizeFilename (for use in BookService)
     */
    public String sanitizeFilenamePublic(String filename) {
        return sanitizeFilename(filename);
    }
}

