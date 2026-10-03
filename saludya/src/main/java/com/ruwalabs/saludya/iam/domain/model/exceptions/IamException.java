package com.ruwalabs.saludya.iam.domain.model.exceptions;

import lombok.Getter;
@Getter
public class IamException extends RuntimeException {
    private final int status;
    private final String code;
    public IamException(int status, String code, String message) {
        super(message); this.status=status; this.code=code;
    }
    public static IamException unauthorized() {
        return new IamException(401,"IAM_INVALID_CREDENTIALS","Invalid credentials or account role");
    }
    public static IamException forbidden() {
        return new IamException(403,"IAM_ACCESS_DENIED","You do not have permission to access this resource");
    }
    public static IamException notFound() {
        return new IamException(404,"IAM_NOT_FOUND","The requested resource was not found");
    }
}
