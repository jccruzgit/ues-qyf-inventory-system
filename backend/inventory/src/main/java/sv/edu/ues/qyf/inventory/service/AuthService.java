package sv.edu.ues.qyf.inventory.service;

import sv.edu.ues.qyf.inventory.dto.LoginRequestDto;
import sv.edu.ues.qyf.inventory.dto.LoginResponseDto;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto request);
}
