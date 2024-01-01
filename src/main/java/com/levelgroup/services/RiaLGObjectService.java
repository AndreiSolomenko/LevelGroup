package com.levelgroup.services;

import com.levelgroup.model.RiaLGObject;
import com.levelgroup.repo.RiaLGObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiaLGObjectService {

    private final RiaLGObjectRepository riaLGObjectRepository;

    public RiaLGObjectService(RiaLGObjectRepository riaLGObjectRepository) {
        this.riaLGObjectRepository = riaLGObjectRepository;
    }


    @Transactional
    public void add(RiaLGObject riaLGObject) {
        riaLGObjectRepository.save(riaLGObject);
    }

    @Transactional
    public RiaLGObject getById(String localRealtyId) {
        return riaLGObjectRepository.findRiaLGObjectByLocalRealtyId(localRealtyId);
    }



    @Transactional
    public Page<RiaLGObject> getObjectsByFilters(
            String typeInXML,
            List<String> categoryInXML,
            List<String> district,
            List<String> newBuildingName,
            Double fromArea,
            Double toArea,
            Double fromPrice,
            Double toPrice,
            List<String> rooms,
            String street,
            List<String> metro,
            List<String> floor,
            Double fromKitchenArea,
            Double toKitchenArea,
            String offerType,
            Pageable pageable) {
        return riaLGObjectRepository.findRiaLGObjectsByFilters(
                typeInXML, categoryInXML, district, newBuildingName, fromArea, toArea, fromPrice, toPrice,
                rooms, street, metro, floor, fromKitchenArea, toKitchenArea, offerType, pageable);
    }


    @Transactional
    public List<RiaLGObject> getDataForFilters(
            String typeInXML,
            List<String> categoryInXML,
            List<String> district,
            List<String> newBuildingName,
            Double fromArea,
            Double toArea,
            Double fromPrice,
            Double toPrice,
            List<String> rooms,
            String street,
            List<String> metro,
            List<String> floor,
            Double fromKitchenArea,
            Double toKitchenArea,
            String offerType) {
        return riaLGObjectRepository.getDataForFilters(
                typeInXML, categoryInXML, district, newBuildingName, fromArea, toArea, fromPrice, toPrice,
                rooms, street, metro, floor, fromKitchenArea, toKitchenArea, offerType);
    }


    @Transactional(readOnly = true)
    public ArrayList<RiaLGObject> get() {

        var objects = riaLGObjectRepository.findAll();
        var result = new ArrayList<RiaLGObject>();

        objects.forEach(object -> result.add(object));
        return result;
    }

}
