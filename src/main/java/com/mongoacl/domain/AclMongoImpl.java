package com.mongoacl.domain;

import com.mongoacl.repository.AclRepository;
import com.mongoacl.repository.ObjectIdentityRepository;
import com.mongoacl.repository.SidRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Satellite on 22.02.2017.
 */
@Document
@EqualsAndHashCode
//TODO: но здесь я не пользуюсь DI??? В модели DI не пользуюсь?
//TODO: какие классы используют спринг а какие нет???
//TODO: нормально ли, что в модели столько вспомогательных методов???
public class AclMongoImpl implements MutableAcl {

    @Id
    @Getter
    private String id;
    //TODO: а эта древовидная структура как-то связана с древовидной структой object identity?
    private String parentAcl;
    private String owner;
    @Setter
    private String objectIdentity;
    private List<String> loadedSids;
    //TODO: это обязательно или можно упустить? проверка на то, что тот,
    //TODO: кто изменяет Acl обладает соответствующими правами - либо является owner,
    //TODO: либо ему предоставлены такие права
    @Transient
    private AclAuthorizationStrategy aclAuthorizationStrategy;
    @Transient
    @Setter
    private PermissionGrantingStrategy permissionGrantingStrategy;
    @Transient
    @Setter
    private SidRepository sidRepository;
    @Transient
    @Setter
    private AclRepository aclRepository;
    @Transient
    @Setter
    private ObjectIdentityRepository objectIdentityRepository;
    private List<AccessControlEntryMongo> aces = new ArrayList<>();
    @Getter
    @Setter
    private boolean entriesInheriting = true;

    public AclMongoImpl() {
    }

    public AclMongoImpl(String objectIdentity, String parentAcl,
                        String owner, SidRepository sidRepository,
                        AclRepository aclRepository,
                        ObjectIdentityRepository objectIdentityRepository,
                                   /* AclAuthorizationStrategy aclAuthorizationStrategy,*/
                        AuditLogger auditLogger) {
        Assert.notNull(objectIdentity, "Object Identity required");
       // Assert.notNull(id, "Id required");
        // Assert.notNull(aclAuthorizationStrategy, "AclAuthorizationStrategy required");
        Assert.notNull(auditLogger, "AuditLogger required");
        this.objectIdentity = objectIdentity;
        this.parentAcl = parentAcl;
        this.owner = owner;
        //this.aclAuthorizationStrategy = aclAuthorizationStrategy;
        this.permissionGrantingStrategy = new DefaultPermissionGrantingStrategy(
                auditLogger);
        this.sidRepository = sidRepository;
        this.aclRepository = aclRepository;
        this.objectIdentityRepository = objectIdentityRepository;
    }


    public void deleteAce(int aceIndex) throws NotFoundException {
       /* aclAuthorizationStrategy.securityCheck(this,
                AclAuthorizationStrategy.CHANGE_GENERAL);*/
        verifyAceIndexExists(aceIndex);

        synchronized (aces) {
            this.aces.remove(aceIndex);
        }
    }

    private void verifyAceIndexExists(int aceIndex) {
        if (aceIndex < 0) {
            throw new NotFoundException("aceIndex must be greater than or equal to zero");
        }
        if (aceIndex >= this.aces.size()) {
            throw new NotFoundException(
                    "aceIndex must refer to an index of the AccessControlEntry list. "
                            + "List size is " + aces.size() + ", index was " + aceIndex);
        }
    }

    public void insertAce(int atIndexLocation, Permission permission, Sid sid,
                          boolean granting) throws NotFoundException {
        /*aclAuthorizationStrategy.securityCheck(this,
                AclAuthorizationStrategy.CHANGE_GENERAL);*/
        Assert.notNull(permission, "Permission required");
        Assert.notNull(sid, "Sid required");
        if (atIndexLocation < 0) {
            throw new NotFoundException(
                    "atIndexLocation must be greater than or equal to zero");
        }
        if (atIndexLocation > this.aces.size()) {
            throw new NotFoundException(
                    "atIndexLocation must be less than or equal to the size of the AccessControlEntry collection");
        }

        AccessControlEntryMongo ace = new AccessControlEntryMongo(null, id, permission,
                sid, false, false, granting);

        synchronized (aces) {
            this.aces.add(atIndexLocation, ace);
        }
    }

    public List<AccessControlEntry> getEntries() {
        return new ArrayList<>(aces);
    }

    public void setEntries(List<AccessControlEntryMongo> aces) {
        this.aces = aces;
    }

    public boolean isSidLoaded(List<Sid> sids) {
        // If loadedSides is null, this indicates all SIDs were loaded
        // Also return true if the caller didn't specify a SID to find
        if ((this.loadedSids == null) || (sids == null) || (sids.size() == 0)) {
            return true;
        }

        // This ACL applies to a SID subset only. Iterate to check it applies.
        for (Sid sid : sids) {
            boolean found = false;

            for (String loadedSid : loadedSids) {
                if (sid.equals(sidRepository.findOne(loadedSid))) {
                    // this SID is OK
                    found = true;

                    break; // out of loadedSids for loop
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Acl getParentAcl() {
        if (aclRepository == null) return null;
        return aclRepository.findOne(parentAcl);
    }

    @Override
    public ObjectIdentity getObjectIdentity() {
        if (objectIdentityRepository == null) return null;
        return objectIdentityRepository.findOne(objectIdentity);
    }

    public void setParent(Acl newParent) {
        /*aclAuthorizationStrategy.securityCheck(this,
                AclAuthorizationStrategy.CHANGE_GENERAL);*/
        Assert.isTrue(newParent == null || !newParent.equals(this),
                "Cannot be the parent of yourself");
        if (newParent != null && newParent instanceof AclMongoImpl &&
                ((AclMongoImpl) newParent).getId() == null)
            this.parentAcl = ((AclMongoImpl) newParent).getId().toString();
    }

    public void setOwner(Sid newOwner) {
       /* aclAuthorizationStrategy.securityCheck(this,
                AclAuthorizationStrategy.CHANGE_OWNERSHIP);*/
        Assert.notNull(newOwner, "Owner required");
        this.owner = ((SidMongoImpl) newOwner).getId();
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

//TODO: если не передаю в конструктор репозиторий, то он null  - получается как-то криво написано????
    public Sid getOwner() {
        if (sidRepository == null) return null;
        return sidRepository.findOne(owner);
    }
    public String getObjectIdentityId() {
        return objectIdentity;
    }
    public String getOwnerId() {
        return owner;
    }
    public String getParentAclId() {
        return parentAcl;
    }

    public boolean isGranted(List<Permission> permission, List<Sid> sids,
                             boolean administrativeMode)
            throws NotFoundException, UnloadedSidException {
        Assert.notEmpty(permission, "Permissions required");
        Assert.notEmpty(sids, "SIDs required");

        if (!this.isSidLoaded(sids)) {
            throw new UnloadedSidException("ACL was not loaded for one or more SID");
        }

        return permissionGrantingStrategy.isGranted(this, permission, sids,
                administrativeMode);
    }

    public void updateAce(int aceIndex, Permission permission) throws NotFoundException {
        verifyAceIndexExists(aceIndex);

        synchronized (aces) {
            AccessControlEntryMongo ace = aces.get(aceIndex);
            ace.setPermission(permission);
        }
    }


}
