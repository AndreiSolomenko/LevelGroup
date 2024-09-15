package com.levelgroup.repo;

import com.levelgroup.model.RiaLGObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface RiaLGObjectRepository extends JpaRepository<RiaLGObject, Long> {

    @Query("SELECT o FROM RiaLGObject o WHERE o.localRealtyId = :localRealtyId")
    RiaLGObject findRiaLGObjectByLocalRealtyId(@Param("localRealtyId") String localRealtyId);


    @Query("SELECT o FROM RiaLGObject o " +
            "WHERE (o.advertType = :typeInXML) " +
            "AND (o.realtyType IN :categoryInXML) " +
            "AND (:district is null or o.district IN :district) " +
            "AND (:newBuildingName is null or o.newBuildingName IN :newBuildingName) " +
            "AND (o.totalArea between :fromArea and :toArea) " +
            "AND (o.price between :fromPrice and :toPrice) " +
            "AND (:rooms is null or o.roomsCount IN :rooms) " +
            "AND (COALESCE(:street, '') = '' or LOWER(o.street) = :street) " +
            "AND (:metro is null or o.metro IN :metro) " +
            "AND (:floor is null or o.floor IN :floor) " +
            "AND (o.kitchenArea between :fromKitchenArea and :toKitchenArea) " +
            "AND (COALESCE(:offerType, '') = '' or o.offerType = :offerType) ")
    Page<RiaLGObject> findRiaLGObjectsByFilters(
            @Param("typeInXML") String typeInXML,
            @Param("categoryInXML") List<String> categoryInXML,
            @Param("district") List<String> district,
            @Param("newBuildingName") List<String> newBuildingName,
            @Param("fromArea") Double fromArea,
            @Param("toArea") Double toArea,
            @Param("fromPrice") Double fromPrice,
            @Param("toPrice") Double toPrice,
            @Param("rooms") List<String> rooms,
            @Param("street") String street,
            @Param("metro") List<String> metro,
            @Param("floor") List<String> floor,
            @Param("fromKitchenArea") Double fromKitchenArea,
            @Param("toKitchenArea") Double toKitchenArea,
            @Param("offerType") String offerType,
            Pageable pageable);


    @Query("SELECT o FROM RiaLGObject o " +
            "WHERE (o.advertType = :typeInXML) " +
            "AND (o.realtyType IN :categoryInXML) " +
            "AND (:district is null or o.district IN :district) " +
            "AND (:newBuildingName is null or o.newBuildingName IN :newBuildingName) " +
            "AND (CAST(o.totalArea as DOUBLE) between :fromArea and :toArea) " +
            "AND (o.price between :fromPrice and :toPrice) " +
            "AND (:rooms is null or o.roomsCount IN :rooms) " +
            "AND (COALESCE(:street, '') = '' or LOWER(o.street) = :street) " +
            "AND (:metro is null or o.metro IN :metro) " +
            "AND (:floor is null or o.floor IN :floor) " +
            "AND (CAST(o.kitchenArea as DOUBLE) between :fromKitchenArea and :toKitchenArea) " +
            "AND (COALESCE(:offerType, '') = '' or o.offerType = :offerType) ")
    List<RiaLGObject> getDataForFilters(
            @Param("typeInXML") String typeInXML,
            @Param("categoryInXML") List<String> categoryInXML,
            @Param("district") List<String> district,
            @Param("newBuildingName") List<String> newBuildingName,
            @Param("fromArea") Double fromArea,
            @Param("toArea") Double toArea,
            @Param("fromPrice") Double fromPrice,
            @Param("toPrice") Double toPrice,
            @Param("rooms") List<String> rooms,
            @Param("street") String street,
            @Param("metro") List<String> metro,
            @Param("floor") List<String> floor,
            @Param("fromKitchenArea") Double fromKitchenArea,
            @Param("toKitchenArea") Double toKitchenArea,
            @Param("offerType") String offerType);
}
