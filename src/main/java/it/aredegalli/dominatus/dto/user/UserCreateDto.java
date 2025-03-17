package it.aredegalli.dominatus.dto.user;

import lombok.Data;

@Data
public class UserCreateDto {

    public String email;
    public String plainPassword;
    public String firstName;
    public String lastName;

}
