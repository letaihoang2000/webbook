package com.example.webbook.service;

import com.example.webbook.dto.UserInfo;
import com.example.webbook.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.webbook.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<UserInfo> getAllUsersInfo() {
        List<User> users = userRepository.findAllDistinctUsers();
        for (User user : users){
            System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername() +
                    ", Email: " + user.getEmail() + ", Mobile: " + user.getMobile() + ", Address: " + user.getAddress());
        }
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
}
