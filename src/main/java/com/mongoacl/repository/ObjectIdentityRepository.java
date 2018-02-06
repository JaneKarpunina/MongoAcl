package com.mongoacl.repository;

import com.mongoacl.domain.ObjectIdentityMongoImpl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by user on 23/11/16.
 */
@Repository
public interface ObjectIdentityRepository extends MongoRepository<ObjectIdentityMongoImpl, String> {
    List<ObjectIdentityMongoImpl> findByTypeAndParent(String type, String parent);
}
