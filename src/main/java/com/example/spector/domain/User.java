package com.example.spector.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data   //Автоматические Getters и Setters
@Entity //Аннотация для Spring, которая указывает что данный класс является сущностью
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    //Идентификатор

    private String username;    //Имя пользователя

    private String password;    //Пароль

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)       //Подгрузка роли
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))   //Поля в таблице для класса Role
    @Enumerated(EnumType.STRING)    //Значения для поля роли типа String
    private Set<Role> roles;   //Роль

    public boolean isAdmin() {  //Метод для проверки прав пользователя, для доступа к доп. функциям
        return roles.contains(Role.ENGINEER);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return getRoles(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
