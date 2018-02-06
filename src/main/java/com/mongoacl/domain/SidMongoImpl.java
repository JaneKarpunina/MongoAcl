package com.mongoacl.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * Created by Satellite on 25.02.2017.
 */
@Data
@Document
public class SidMongoImpl implements Sid {

    @Id
    private String id;
    private String sidName;
    private boolean isPrincipal;

    public SidMongoImpl() {}

    public SidMongoImpl(Authentication authentication) {
        Assert.notNull(authentication, "Authentication required");
        Assert.notNull(authentication.getPrincipal(), "Principal required");

        if (authentication.getPrincipal() instanceof UserDetails) {
            this.sidName = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        else {
            this.sidName = authentication.getPrincipal().toString();
        }

        this.isPrincipal = true;
    }
}
