package com.qprogramming.activtytracker.exceptions.dto;

import javax.ws.rs.core.Response;

public class ErrorMessage {
    private String message;
    private Response.Status code;

    public ErrorMessage(Response.Status code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Response.Status getCode() {
        return code;
    }

    public void setCode(Response.Status code) {
        this.code = code;
    }
}
