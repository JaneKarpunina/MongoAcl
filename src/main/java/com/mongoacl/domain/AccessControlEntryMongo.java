package com.mongoacl.domain;

import lombok.*;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 * Created by Satellite on 25.02.2017.
 */
@Data
public class AccessControlEntryMongo implements AccessControlEntry {

    private String id;
    private String acl;
    private Permission permission;
    private Sid sid;
    private boolean auditFailure = false;
    private boolean auditSuccess = false;
    private boolean granting;

    public AccessControlEntryMongo() {}

    public AccessControlEntryMongo(String id, String acl, Permission permission,
                                   Sid sid, boolean auditFailure, boolean auditSuccess,
                                   boolean granting) {
        this.id = id;
        this.acl = acl;
        this.permission = permission;
        this.sid = sid;
        this.auditFailure = auditFailure;
        this.auditSuccess = auditSuccess;
        this.granting = granting;
    }

    public Acl getAcl() {
        return null;
    }
}
