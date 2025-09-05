package com.example.webbook.service;

import com.example.webbook.dto.AddUserForm;
import com.example.webbook.dto.UserInfo;
import com.example.webbook.exception.EmailAlreadyExistsException;
import com.example.webbook.model.Role;
import com.example.webbook.repository.RoleRepository;
import com.example.webbook.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.webbook.model.User;

import java.time.LocalDateTime;
import java.util.List;
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

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public List<UserInfo> getAllUsersInfo() {
        List<User> users = userRepository.findUsersExcludingAdmin();
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = modelMapper.map(user, UserInfo.class);
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

            // Generate username from first name and last name
            String username = addUserForm.getFirst_name() + addUserForm.getLast_name();
            user.setUsername(username);

            // Set other fields
            user.setEmail(addUserForm.getEmail());
            user.setMobile(addUserForm.getMobile());
            user.setAddress(addUserForm.getAddress());
//        user.setPassword(passwordEncoder.encode(addUserForm.getPassword()));
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
}
