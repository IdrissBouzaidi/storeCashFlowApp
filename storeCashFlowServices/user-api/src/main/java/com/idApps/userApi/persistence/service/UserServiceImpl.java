package com.idApps.userApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.userApi.models.dto.UserDto;
import com.idApps.userApi.persistence.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserDto getUserDetails(String username, List<String> fields) throws Exception {

        if(username != null) {
            if(fields ==  null || fields.isEmpty())
                return this.objectMapper.convertValue(this.userRepository.getUserByMail(username), UserDto.class);
            else {
                Collections.replaceAll(fields, "firstName", "first_name");
                Collections.replaceAll(fields, "lastName", "last_name");
                String selectedFields = String.join(", ", fields);
                String sql = "SELECT " + selectedFields + " FROM user WHERE username=:username;";
                Query query = entityManager.createNativeQuery(sql, Map.class);
                query.setParameter("username", username);
                // return (UserEntity) query.getSingleResult();
                System.out.println("result value: " + query.getSingleResult());
                return this.objectMapper.convertValue(query.getSingleResult(), UserDto.class);
            }
        }
        else {
            throw new Exception("USERNAME_NOT_FOUND");
        }
    }
}
