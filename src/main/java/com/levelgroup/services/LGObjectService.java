package com.levelgroup.services;

import com.levelgroup.repo.LGObjectRepository;
import com.levelgroup.model.LGObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

@Service
public class LGObjectService {

    private final LGObjectRepository lgObjectRepository;

    public LGObjectService(LGObjectRepository lgObjectRepository) {
        this.lgObjectRepository = lgObjectRepository;
    }

    @Transactional
    public void add(LGObject lgObject) {
        lgObjectRepository.save(lgObject);
    }

    @Transactional
    public LGObject getById(String internalId) {
       return lgObjectRepository.findLGObjectByInternalId(internalId);
    }

    @Transactional(readOnly = true)
    public ArrayList<LGObject> get() {

        var objects = lgObjectRepository.findAll();
        var result = new ArrayList<LGObject>();

        objects.forEach(object -> result.add(object));
        return result;
    }
}
