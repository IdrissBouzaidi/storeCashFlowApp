package com.idApps.userApi.persistence.service;

import com.idApps.userApi.models.dto.ActionDto;
import com.idApps.userApi.persistence.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActionServiceImpl implements ActionService {

    @Autowired
    private ActionRepository actionRepository;

    @Override
    public List<ActionDto> getProfilesActions(List<Integer> profilesIds) {
        List<Map<String, Object>> resultMapList = this.actionRepository.getprofilesActions(profilesIds);
        return resultMapList.stream().map(ActionDto::instanciateFromMap).toList();
    }
}
