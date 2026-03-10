package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.UserLoginRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.AuthResponse;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.User;

public interface AuthService {

    // Authentification de base
    AuthResponse login(UserLoginRequest userLoginRequest);

    // Inscription d'un nouvel utilisateur
    AuthResponse register(UserRequest userRequest);

    // Récupération de l'utilisateur connecté (Utile pour la logique métier interne)
    User getAuthenticatedUserEntity();

    // Récupération du profil utilisateur (Utile pour l'affichage Frontend)
    UserResponse getAuthenticatedUserResponse();

    // Optionnel : Pour rafraîchir le token sans se reconnecter
    AuthResponse refreshToken(String refreshToken);
}
