package com.idApps.userApi.persistence.service;

import com.idApps.userApi.models.dto.UserDto;

import java.util.List;

public interface UserService{
    UserDto getUserDetails(String username, List<String> fields) throws Exception;
}
