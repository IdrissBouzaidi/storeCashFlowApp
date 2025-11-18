package com.idApps.storeCashFlowApi.help.constants;

public interface TransactionType {

    // --- Création ---
    Integer CREATE_CONS_INP = 1;
    Integer CREATE_NOT_CONS_INP = 2;
    Integer CREATE_OUT = 3;
    Integer CREATE_CHAR = 4;
    Integer CREATE_CONTR_CAPITAL = 5;
    Integer CREATE_ADV = 6;
    Integer CREATE_OUT_POCKET = 7;
    Integer CREATE_CUSTOMER_CREDIT = 8;
    Integer CREATE_EXTERNAL_LOAN = 9;

    // --- Mise à jour ---
    Integer UPDATE_CONS_INP = 10;
    Integer CANCEL_CONS_INP = 11;
    Integer UPDATE_NOT_CONS_INP = 12;
    Integer CANCEL_NOT_CONS_INP = 13;
    Integer UPDATE_OUT = 14;
    Integer CANCEL_OUT = 15;
    Integer UPDATE_CHAR = 16;
    Integer CANCEL_CHAR = 17;
    Integer UPDATE_CONTR_CAPITAL = 18;
    Integer CANCEL_CONTR_CAPITAL = 19;
    Integer UPDATE_ADV = 20;
    Integer CANCEL_ADV = 21;
    Integer UPDATE_OUT_POCKET = 22;
    Integer CANCEL_OUT_POCKET = 23;
    Integer UPDATE_CUSTOMER_CREDIT = 24;
    Integer CANCEL_CUSTOMER_CREDIT = 25;
    Integer UPDATE_EXTERNAL_LOAN = 26;
    Integer CANCEL_EXTERNAL_LOAN = 27;

    // --- Périodes financières ---
    Integer CREATE_PERIOD = 28;
    Integer UPDATE_PERIOD = 29;
    Integer CANCEL_PERIOD = 30;
    Integer CLOSE_PERIOD = 31;
    Integer REOPEN_PERIOD = 32;
    Integer CREATE_PRODUCT = 33;
    Integer UPDATE_PRODUCT = 34;
    Integer CANCEL_PRODUCT = 35;
}

