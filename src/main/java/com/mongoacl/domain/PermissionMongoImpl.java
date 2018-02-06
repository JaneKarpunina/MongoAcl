package com.mongoacl.domain;


import lombok.Data;
import org.springframework.security.acls.model.Permission;

/**
 * Created by Satellite on 25.02.2017.
 */
@Data
public class PermissionMongoImpl implements Permission {
    protected String pattern;
    protected int mask;
}
