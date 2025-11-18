package com.idApps.userApi.persistence.service;

import com.idApps.userApi.models.dto.ActionDto;

import java.util.List;

public interface ActionService {
    List<ActionDto> getProfilesActions(List<Integer> profilesIds);
}
