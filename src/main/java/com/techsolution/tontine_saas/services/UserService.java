package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getUsersByAssociation();
    UserResponse updateUserStatus(Long id, boolean active);
    void deleteUser(Long id);

}
