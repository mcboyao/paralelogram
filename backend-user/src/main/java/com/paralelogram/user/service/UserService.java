package com.paralelogram.user.service;


import com.paralelogram.user.entity.User;
import com.paralelogram.user.model.AddUserRequest;

public interface UserService {

    User getUser(String userName);

    User addUser(AddUserRequest user);

}
