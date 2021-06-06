package com.wq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

// lombok包
@Data
@AllArgsConstructor
public class Person {
    private String lastName;
    private String firstName;

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName;
    }
}
