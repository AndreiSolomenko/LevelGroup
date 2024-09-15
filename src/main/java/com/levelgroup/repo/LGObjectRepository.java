package com.levelgroup.repo;

import com.levelgroup.model.LGObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface LGObjectRepository extends JpaRepository<LGObject, Long> {

    @Query("SELECT o FROM LGObject o WHERE o.internalId = :internalId")
    LGObject findLGObjectByInternalId(@Param("internalId") String internalId);

}
