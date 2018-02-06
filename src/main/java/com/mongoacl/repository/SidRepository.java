package com.mongoacl.repository;

import com.mongoacl.domain.SidMongoImpl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Satellite on 10.03.2017.
 */
@Repository
public interface SidRepository extends MongoRepository<SidMongoImpl, String> {

    SidMongoImpl findBySidNameAndIsPrincipal(String sidName, boolean isPrincipal);
}
