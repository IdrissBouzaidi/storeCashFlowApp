package com.idApps.userApi.persistence.service;

import com.idApps.userApi.models.response.ProfilResponse;
import com.idApps.userApi.persistence.entity.ProfilEntity;

import java.util.List;

public interface ProfilService {
    List<ProfilEntity> getUserProfils(String username);

    List<ProfilResponse> getUserActions(String username);
}
