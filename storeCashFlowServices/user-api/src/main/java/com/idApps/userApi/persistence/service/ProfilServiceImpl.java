package com.idApps.userApi.persistence.service;

import com.idApps.userApi.models.dto.ActionDto;
import com.idApps.userApi.models.response.ActionResponse;
import com.idApps.userApi.models.response.ProfilResponse;
import com.idApps.userApi.persistence.entity.ProfilEntity;
import com.idApps.userApi.persistence.repository.ProfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfilServiceImpl implements ProfilService {

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private ActionService actionService;

    @Override
    public List<ProfilEntity> getUserProfils(String username) {
        List<ProfilEntity> userProfiles = this.profilRepository.getUserProfiles(username);
        return userProfiles;
    }

    @Override
    public List<ProfilResponse> getUserActions(String username) {
        List<ProfilEntity> userProfils = this.getUserProfils(username);
        List<Integer> profilsIds = userProfils.stream().map(profil -> profil.getId()).collect(Collectors.toList());
        List<ActionDto> profilsActions = this.actionService.getProfilesActions(profilsIds);
        List<ProfilResponse> userActions = userProfils.stream().map(ProfilEntity::mapToProfilResponse).sorted((item1, item2) -> item1.getId()-item2.getId()).toList();
        userActions.forEach(profil -> profil.setActions(new ArrayList<>()));
        profilsActions.forEach(profilAction -> {
            ProfilResponse profil = userActions.stream().filter(item -> item.getId().equals(profilAction.getIdProfil())).findFirst().get();
            profil.getActions().add(ActionResponse.instanciateFromActionDto(profilAction));
        });
        return userActions;
    }
}
