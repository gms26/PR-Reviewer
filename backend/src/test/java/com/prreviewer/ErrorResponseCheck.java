package com.prreviewer;

import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.NoHandlerFoundException;

public class ErrorResponseCheck {
    public static void main(String[] args) {
        System.out.println("NoResourceFoundException is ErrorResponse: " + 
            ErrorResponse.class.isAssignableFrom(NoResourceFoundException.class));
        System.out.println("NoHandlerFoundException is ErrorResponse: " + 
            ErrorResponse.class.isAssignableFrom(NoHandlerFoundException.class));
    }
}
