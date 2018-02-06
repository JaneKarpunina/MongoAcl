package com.mongoacl.service.impl;

import com.mongoacl.domain.AccessControlEntryMongo;
import com.mongoacl.domain.AclMongoImpl;
import com.mongoacl.domain.ObjectIdentityMongoImpl;
import com.mongoacl.domain.SidMongoImpl;
import com.mongoacl.repository.AclRepository;
import com.mongoacl.repository.ObjectIdentityRepository;
import com.mongoacl.repository.SidRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 22/11/16.
 */
@Service
public class MongodbMutableAclService extends MongodbAclService implements MutableAclService {

    private SidRepository sidRepository;
    private AuditLogger auditLogger;

    @Autowired
    public MongodbMutableAclService(ObjectIdentityRepository objectIdentityRepository,
                                    AclRepository aclRepository,
                                    SidRepository sidRepository,
                                    AuditLogger auditLogger) {
        super(objectIdentityRepository, aclRepository);
        this.sidRepository = sidRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Creates an empty <code>Acl</code> object in the database. It will have no entries.
     * The returned object will then be used to add entries.
     *
     * @param objectIdentity the object identity to create
     * @return an ACL object with its ID set
     * @throws AlreadyExistsException if the passed object identity already has a record
     */
    public MutableAcl createAcl(ObjectIdentity objectIdentity) throws AlreadyExistsException {
        // Check this object identity hasn't already been persisted
        if (objectIdentity.getIdentifier() != null && objectIdentityRepository.
                findOne(((ObjectIdentityMongoImpl) objectIdentity).getIdentifier()) != null)
                 {
            throw new AlreadyExistsException("Object identity '" + objectIdentity
                    + "' already exists");
        }

        // Need to retrieve the current principal, in order to know who "owns" this ACL
        // (can be changed later on)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SidMongoImpl sid = new SidMongoImpl(auth);

        createObjectIdentity(objectIdentity, sid);
        Acl acl = readAclById(objectIdentity);
        Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be been returned");

        return (MutableAcl) acl;
    }

    private void createObjectIdentity(ObjectIdentity objectIdentity, SidMongoImpl sid) {
        SidMongoImpl foundSid = sidRepository.
                findBySidNameAndIsPrincipal(sid.getSidName(), sid.isPrincipal());
        if (foundSid == null) {
            foundSid = sid;
            sidRepository.insert(foundSid);
        }
        ObjectIdentityMongoImpl objectIdentityMongo = objectIdentityRepository.insert((ObjectIdentityMongoImpl) objectIdentity);
        aclRepository.insert
                (new AclMongoImpl(objectIdentityMongo.getIdentifier(), null, foundSid.getId(),
                        null, null, null, auditLogger));
    }


    public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren)
            throws ChildrenExistException {
        List<ObjectIdentity> children = findChildren(objectIdentity);
        if (children != null) {
            for (ObjectIdentity child : children) {
                if (deleteChildren) deleteAcl(child, true);
                else {
                    ((ObjectIdentityMongoImpl) child).setParent(null);
                    objectIdentityRepository.save((ObjectIdentityMongoImpl) child); //updates an object as well
                }
            }
        }
        AclMongoImpl acl = aclRepository.
                findByObjectIdentity((((ObjectIdentityMongoImpl) objectIdentity).getIdentifier()));
        aclRepository.delete(acl);
        //проставление у всех дочерних родителя нулем
        aclRepository.findByParentAcl(acl.getId()).forEach(e ->  {
            e.setParent(acl);
            aclRepository.save(e);
        });
        objectIdentityRepository.delete((ObjectIdentityMongoImpl) objectIdentity);
        //Todo: проставить sid репозиторий здесь - это нормально????
        acl.setSidRepository(sidRepository);
        SidMongoImpl sid = (SidMongoImpl) acl.getOwner();
        List<AclMongoImpl> aclMongos = aclRepository.findByOwner(sid.getId());
        if (aclMongos == null || aclMongos.isEmpty())
            sidRepository.delete(sid);
    }

    /**
     * Changes an existing <code>Acl</code> in the database.
     * Replaces ACEs of the AclMongoImpl with the same id as the parameter
     * @param acl to modify
     * @throws NotFoundException if the relevant record could not be found (did you
     *                           remember to use {@link #createAcl(ObjectIdentity)} to create the object, rather
     *                           than creating it with the <code>new</code> keyword?)
     */
    public MutableAcl updateAcl(MutableAcl acl) throws NotFoundException {
        Assert.notNull(acl.getId(), "Object Identity doesn't provide an identifier");
        AclMongoImpl foundAcl = aclRepository.findOne(((AclMongoImpl)acl).getId());
        if (foundAcl == null) return createAcl(objectIdentityRepository.
                findOne(((ObjectIdentityMongoImpl)acl.getObjectIdentity()).getIdentifier()));
        List<AccessControlEntryMongo> aces = new ArrayList<>();
        acl.getEntries().forEach(e ->
                        aces.add((AccessControlEntryMongo) e));
        foundAcl.setEntries(aces);
        return aclRepository.save(foundAcl);
    }
}
