package com.idApps.userApi.persistence.repository;

import com.idApps.userApi.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query(value = "SELECT * FROM user WHERE username=:username"
            , nativeQuery = true)
    UserEntity getUserByMail(@Param("username") String username);

}
