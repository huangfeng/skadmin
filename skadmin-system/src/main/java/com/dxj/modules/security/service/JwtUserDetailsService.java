package com.dxj.modules.security.service;

import com.dxj.exception.BadRequestException;
import com.dxj.modules.system.domain.*;
import com.dxj.exception.EntityNotFoundException;
import com.dxj.modules.security.domain.JwtUser;
import com.dxj.modules.system.dto.DeptDTO;
import com.dxj.modules.system.dto.JobDTO;
import com.dxj.modules.system.dto.UserDTO;
import com.dxj.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author dxj
 * @date 2018-11-22
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    private final JwtPermissionService permissionService;

    @Autowired
    public JwtUserDetailsService(UserService userService, JwtPermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username){

        UserDTO user = userService.findByName(username);
        if (user == null) {
            throw new BadRequestException("账号不存在");
        } else {
            return createJwtUser(user);
        }
    }

    public UserDetails createJwtUser(UserDTO user) {
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getAvatar(),
                user.getEmail(),
                user.getPhone(),
                Optional.ofNullable(user.getDept()).map(DeptDTO::getName).orElse(null),
                Optional.ofNullable(user.getJob()).map(JobDTO::getName).orElse(null),
                permissionService.mapToGrantedAuthorities(user),
                user.getEnabled(),
                user.getCreateTime(),
                user.getLastPasswordResetTime()
        );
    }
}
