package com.mongoacl;

import com.mongoacl.config.MongoTestsConfig;
import com.mongoacl.domain.AccessControlEntryMongo;
import com.mongoacl.domain.AclMongoImpl;
import com.mongoacl.domain.ObjectIdentityMongoImpl;
import com.mongoacl.domain.SidMongoImpl;
import com.mongoacl.repository.AclRepository;
import com.mongoacl.repository.ObjectIdentityRepository;
import com.mongoacl.repository.SidRepository;
import com.mongoacl.service.impl.MongodbAclService;
import com.mongoacl.service.impl.MongodbMutableAclService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
//@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoTestsConfig.class)
@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("javax.management.*")
public class MongoAclApplicationTests {


    @Autowired
    MongodbMutableAclService mongodbMutableAclService;

    @Autowired
    AclRepository aclRepository;

    @Autowired
    ObjectIdentityRepository objectIdentityRepository;

    @Autowired
    SidRepository sidRepository;

    @Mock
    SecurityContextImpl securityContext;

    @Mock
    AnonymousAuthenticationToken authenticationToken;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createAclTest() {
        PowerMockito.mockStatic(SecurityContextHolder.class);
        PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getPrincipal()).thenReturn("test1");
        AclMongoImpl aclMongo = (AclMongoImpl) mongodbMutableAclService.
                createAcl(createObjectIdentity("id_test1", "parent_test1", "type_test1"));
        assertTrue("create acl test failed: ids are not equal", "id_test1".equals(
                aclMongo.getObjectIdentityId()));
    }

    @Test
    public void deleteAclTest() {
        //проверить удаление дочерних и сидов в случае необходимости
        ObjectIdentityMongoImpl identityMongo = createObjectIdentity("id2", null, "type1");
        saveObjectIdentity(identityMongo);
        prepareDelete();
        mongodbMutableAclService.deleteAcl(identityMongo, true);
        isAclNull("id2");
        isAclNull("id3");
        isAclNull("id4");
    }

    @Test
    public void updateAclTest() {
        saveObjectIdentity(createObjectIdentity("id1", null, "type1"));
        AclMongoImpl aclMongo = createAcl("id1", "sid1");
        saveAcl(aclMongo);
        saveSid(createSid("sid1", "s1", true));
        List<AccessControlEntryMongo> aces = new ArrayList<>();
        addAce("id1", aces);
        addAce("id2", aces);
        aclMongo.setEntries(aces);
        mongodbMutableAclService.updateAcl(aclMongo);

        assertTrue("Acl was not updated",
                aclRepository.findByObjectIdentity("id1").getEntries().size() == 2);
    }

    private void addAce(String id, List<AccessControlEntryMongo> aces) {
        AccessControlEntryMongo ace = new AccessControlEntryMongo();
        ace.setId(id);
        aces.add(ace);
    }


    private void isAclNull(String id) {
        assertTrue(id + "is not deleted", aclRepository.findByObjectIdentity(id) == null);
    }

    private void prepareDelete() {
        saveObjectIdentity(createObjectIdentity("id3", "id2", "type1"));
        saveObjectIdentity(createObjectIdentity("id4", "id2", "type1"));

        saveAcl(createAcl("id2", "sid1"));
        saveAcl(createAcl("id3", "sid2"));
        saveAcl(createAcl("id4", "sid1"));
        saveSid(createSid("sid1", "s1", true));
        saveSid(createSid("sid2", "s2", false));
    }

    private ObjectIdentityMongoImpl createObjectIdentity(String id, String parent, String type) {
        ObjectIdentityMongoImpl objectIdentityMongo = new ObjectIdentityMongoImpl();
        objectIdentityMongo.setIdentifier(id);
        objectIdentityMongo.setParent(parent);
        objectIdentityMongo.setType(type);
        return objectIdentityMongo;
    }

    private AclMongoImpl createAcl(String objectIdentity, String sid) {
        AclMongoImpl aclMongo = new AclMongoImpl();
        aclMongo.setObjectIdentity(objectIdentity);
        aclMongo.setOwner(sid);
        return aclMongo;
    }

    private SidMongoImpl createSid(String id, String name, boolean principal) {
        SidMongoImpl sidMongo = new SidMongoImpl();
        sidMongo.setId(id);
        sidMongo.setSidName(name);
        sidMongo.setPrincipal(principal);
        return sidMongo;
    }

    private void saveAcl(AclMongoImpl aclMongo) {
        aclRepository.save(aclMongo);
    }

    private void saveObjectIdentity(ObjectIdentityMongoImpl identityMongo) {
        objectIdentityRepository.save(identityMongo);
    }

    private void saveSid(SidMongoImpl sidMongo) {
        sidRepository.save(sidMongo);
    }
}
