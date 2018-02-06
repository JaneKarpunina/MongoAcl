package com.mongoacl.repository;

import com.mongoacl.domain.AclMongoImpl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by user on 24/11/16.
 */
@Repository
public interface AclRepository extends MongoRepository<AclMongoImpl, String> {

    AclMongoImpl findByLoadedSidsAndObjectIdentity(String objectIdentity, List<Sid> loadedSids);
    AclMongoImpl findByObjectIdentity(String objectIdentity);
    AclMongoImpl deleteByObjectIdentity(String objectIdentity);
    List<AclMongoImpl> findByOwner(String owner);
    List<AclMongoImpl> findByParentAcl(String parentAcl);

}
