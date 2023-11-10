package com.example.spector.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {  //Импликация интерфейса GrantedAuthority
                                                //для взятия значений ролей
    TECHNICIAN, ENGINEER;   //Роли инженер (ADMIN) и техник (USER)

    @Override
    public String getAuthority() { return name(); }     //Переопределение метода getAuthority
                                                        //интерфейса GrantedAuthority,
                                                        //для подтягивания роли по имени пользователя
}
