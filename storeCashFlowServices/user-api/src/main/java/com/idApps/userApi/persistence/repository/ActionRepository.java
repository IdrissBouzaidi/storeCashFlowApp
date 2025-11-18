package com.idApps.userApi.persistence.repository;

import com.idApps.userApi.persistence.entity.ActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, Integer> {

    @Query(value = """
            SELECT action.*, profil_action.id_profil AS id_profil FROM action
            JOIN profil_action ON profil_action.id_action=action.id
             WHERE profil_action.id_profil IN :profilsIds
            """, nativeQuery = true)
    List<Map<String, Object>> getprofilesActions(List<Integer> profilsIds);
}
