package com.learningjava.learningRestApi.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.logging.log4j.message.Message;

@Data
public class AddStudentRequestDto {

    @NotBlank(message = "Email should not be blank")
    @Size(max = 20 , min = 3, message = "Name should be of minimum of 3 char and maximum of 30")
    private String name;

    @Email
    @NotBlank(message = "Email is Required")
    private String email;
}
