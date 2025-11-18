package com.idApps.storeCashFlowApi.help.constants;

/**
 * Représente les différents états possibles d’un produit dans le cycle de vente du magasin.
 */
public interface ProductTransactionState {

    // 🟢 Produit disponible à la vente
    int AVAILABLE = 1;

    // 🟡 Produit réservé par un client
    int RESERVED = 2;

    // 🟠 Produit vendu
    int SOLD = 3;

    // 🔵 Vente annulée (avant ou après la transaction)
    int CANCELED = 4;

    // 🔁 Produit retourné après vente
    int RETURNED = 5;

    // 🔴 Produit perdu ou manquant dans le stock
    int LOST = 6;
}