package sv.edu.ues.qyf.inventory.service;

import sv.edu.ues.qyf.inventory.entity.User;

public interface CurrentUserService {

    User getAuthenticatedUser();

    User getAuthenticatedUserOrNull();
}
