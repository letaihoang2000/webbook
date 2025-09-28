package com.example.webbook.service;

import com.example.webbook.dto.AddUserForm;
import com.example.webbook.dto.UpdateUserForm;
import com.example.webbook.dto.UserInfo;
import com.example.webbook.exception.EmailAlreadyExistsException;
import com.example.webbook.model.Role;
import com.example.webbook.repository.RoleRepository;
import com.example.webbook.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.webbook.model.User;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserInfo> getAllUsersInfo() {
        List<User> users = userRepository.findUsersExcludingAdmin();
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    // Paginated method with search support
    public Page<UserInfo> getUsersInfoPaginated(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<User> userPage;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            userPage = userRepository.findUsersExcludingAdminWithSearch(searchQuery.trim(), pageable);
        } else {
            userPage = userRepository.findUsersExcludingAdmin(pageable);
        }

        return userPage.map(this::convertToUserInfo);
    }

    public Page<UserInfo> getUsersInfoPaginated(int page, int size) {
        return getUsersInfoPaginated(page, size, null);
    }

    // Get pagination info with search support
    public Map<String, Object> getPaginationInfo(int currentPage, int pageSize, String searchQuery) {
        long totalUsers;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            totalUsers = userRepository.countUsersExcludingAdminWithSearch(searchQuery.trim());
        } else {
            totalUsers = userRepository.countUsersExcludingAdmin();
        }

        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        Map<String, Object> paginationInfo = new HashMap<>();
        paginationInfo.put("currentPage", currentPage);
        paginationInfo.put("pageSize", pageSize);
        paginationInfo.put("totalUsers", totalUsers);
        paginationInfo.put("totalPages", totalPages);
        paginationInfo.put("hasPrevious", currentPage > 0);
        paginationInfo.put("hasNext", currentPage < totalPages - 1);
        paginationInfo.put("searchQuery", searchQuery);

        return paginationInfo;
    }

    public Map<String, Object> getPaginationInfo(int currentPage, int pageSize) {
        return getPaginationInfo(currentPage, pageSize, null);
    }

    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = modelMapper.map(user, UserInfo.class);
        userInfo.setUser_id(user.getId().toString());

        // Format last_updated date to string
        if (user.getLast_updated() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            userInfo.setLast_updated(user.getLast_updated().format(formatter));
        }

        // Map role name specifically since it's nested
        if (user.getRole() != null) {
            userInfo.setRole_name(user.getRole().getRoleName());
        }
        return userInfo;
    }

    public User createUser(AddUserForm addUserForm) {
        // Check if email already exists
        if (userRepository.existsByEmail(addUserForm.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Role userRole = roleRepository.findByRoleName("USER");
        if (userRole == null) {
            throw new RuntimeException("User role not found in the system. Please contact administrator.");
        }

        try {
            User user = new User();
            user.setId(UUID.randomUUID());

            // Handle image upload
            MultipartFile imageFile = addUserForm.getImageFile();
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageUrl = imageUploadService.uploadImage(imageFile,user.getId());
                    user.setImage(imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid image file: " + e.getMessage());
                }
            }

            user.setFirst_name(addUserForm.getFirst_name());
            user.setLast_name(addUserForm.getLast_name());
            user.setEmail(addUserForm.getEmail());
            user.setMobile(addUserForm.getMobile());
            user.setAddress(addUserForm.getAddress());
            user.setPassword(passwordEncoder.encode(addUserForm.getPassword()));
            user.setPassword(addUserForm.getPassword());

            user.setRole(userRole);

            // Set timestamps
            user.setCreated_at(LocalDateTime.now());
            user.setLast_updated(LocalDateTime.now());

            return userRepository.save(user);
        } catch(Exception e){
            throw new RuntimeException("Failed to add user: " + e.getMessage());
        }
    }

    // Update user
    public User updateUser(UpdateUserForm updateUserForm) {
        try {
            UUID userId = UUID.fromString(updateUserForm.getId());
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Update username if name fields changed
            existingUser.setFirst_name(updateUserForm.getFirst_name());
            existingUser.setLast_name(updateUserForm.getLast_name());

            // Update fields (email remains unchanged)
            existingUser.setMobile(updateUserForm.getMobile());
            existingUser.setAddress(updateUserForm.getAddress());

            // Update password only if provided
            if (updateUserForm.getPassword() != null && !updateUserForm.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updateUserForm.getPassword()));
            }

            // Handle image upload if provided
            MultipartFile imageFile = updateUserForm.getImageFile();
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    // Delete old image if exists
                    if (existingUser.getImage() != null && !existingUser.getImage().isEmpty()) {
                        imageUploadService.deleteImage(existingUser.getImage());
                    }

                    // Upload new image
                    String imageUrl = imageUploadService.uploadImage(imageFile, existingUser.getId());
                    existingUser.setImage(imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid image file: " + e.getMessage());
                }
            }

            // Update timestamp
            existingUser.setLast_updated(LocalDateTime.now());

            return userRepository.save(existingUser);
        } catch(Exception e){
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    public void deleteUser(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            User user = userRepository.findById(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Delete user's image from Cloudinary if exists
            if (user.getImage() != null && !user.getImage().trim().isEmpty()) {
                try {
                    imageUploadService.deleteImage(user.getImage());
                } catch (Exception e) {
                    // Log the error but don't fail the user deletion
                    System.err.println("Warning: Failed to delete image for user " + userId + ": " + e.getMessage());
                }
            }

            // Delete user from database
            userRepository.delete(user);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid user ID format: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
}
