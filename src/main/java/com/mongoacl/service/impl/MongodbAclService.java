package com.mongoacl.service.impl;

import com.mongoacl.domain.AclMongoImpl;
import com.mongoacl.domain.ObjectIdentityMongoImpl;
import com.mongoacl.repository.AclRepository;
import com.mongoacl.repository.ObjectIdentityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by user on 22/11/16.
 */
@Service
public class MongodbAclService implements AclService {
    // ~ Methods
    // ========================================================================================================

    ObjectIdentityRepository objectIdentityRepository;
    AclRepository aclRepository;
    @Autowired
    public MongodbAclService(ObjectIdentityRepository objectIdentityRepository,
                             AclRepository aclRepository) {
        super();
        this.objectIdentityRepository = objectIdentityRepository;
        this.aclRepository = aclRepository;
    }

    public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
        List<ObjectIdentity> objects = new ArrayList<>();
        objectIdentityRepository.findByTypeAndParent(parentIdentity.getType(),
                ((ObjectIdentityMongoImpl) parentIdentity).getIdentifier()).
                forEach(e -> objects.add(e));
        if (objects.size() == 0) {
            return null;
        }
        return objects;
    }

    public Acl readAclById(ObjectIdentity object) throws NotFoundException {
        return aclRepository.findByObjectIdentity(((ObjectIdentityMongoImpl) object).getIdentifier());
    }

    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
        return readAclsById(Arrays.asList(object), sids).get(object);
    }

    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects)
            throws NotFoundException {
        return readAclsById(objects, null);
    }

    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids)
            throws NotFoundException {
        Map<ObjectIdentity, Acl> acls = new HashMap<>();
        objects.forEach(o -> {
            AclMongoImpl acl = aclRepository.
                    findByObjectIdentity(((ObjectIdentityMongoImpl) o).getIdentifier());
            if (acl.isSidLoaded(sids))
                acls.put(o, acl);
        });
        objects.forEach(o -> {
            if (!acls.containsKey(o))
                throw new NotFoundException(
                        "Unable to find ACL information for object identity '" + o
                                + "'");

        });
        return acls;
    }
}
