package com.studyIn.modules.account.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailForm {

    @NotBlank @Email
    private String email;
}
