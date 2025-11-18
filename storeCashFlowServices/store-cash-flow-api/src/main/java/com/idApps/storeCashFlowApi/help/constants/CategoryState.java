package com.idApps.storeCashFlowApi.help.constants;

public interface CategoryState {

    // 🟢 Catégorie active et visible
    int ACTIVE = 1;

    // ⚪ Catégorie désactivée, non visible mais conservée
    int INACTIVE = 2;

    // 🔴 Catégorie annulée ou supprimée logiquement
    int CANCELED = 3;
}
