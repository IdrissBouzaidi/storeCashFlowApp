package com.idApps.userApi.persistence.repository;

import com.idApps.userApi.persistence.entity.ProfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfilRepository extends JpaRepository<ProfilEntity, Integer> {

    @Query(value = """
        SELECT * FROM profil WHERE id IN
            ( SELECT id_profil FROM user_profil WHERE id_user IN
                ( SELECT id FROM user WHERE username=:username )
            )
        """, nativeQuery = true)
    List<ProfilEntity> getUserProfiles(@Param("username") String username);

}
