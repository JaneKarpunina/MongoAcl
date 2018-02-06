package com.mongoacl.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * Created by user on 23/11/16.
 */
@Data
@Document
public class ObjectIdentityMongoImpl implements ObjectIdentity {

    @Id
    private String identifier;
    private String parent;
    private String type;
}
