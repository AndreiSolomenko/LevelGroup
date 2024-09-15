package com.levelgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelgroup.SingleObject;
import com.levelgroup.model.LGObject;
import com.levelgroup.model.RiaLGObject;
import com.levelgroup.services.LGObjectService;
import com.levelgroup.services.RiaLGObjectService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class ObjectsController {

    static final int ITEMS_PER_PAGE = 12;
    private final LGObjectService objectService;
    private final RiaLGObjectService riaLGObjectService;

    public ObjectsController(LGObjectService objectService, RiaLGObjectService riaLGObjectService) {
        this.objectService = objectService;
        this.riaLGObjectService = riaLGObjectService;
    }

    @GetMapping("/{type}/{category}/{id}")
    public ResponseEntity<?> getObject(@PathVariable String id) {
        LGObject oneLGObject = objectService.getById(id);
        RiaLGObject oneRiaLGObject = riaLGObjectService.getById(id);

        SingleObject singleObject = new SingleObject();

        singleObject.setSalesAgentName(oneLGObject.getSalesAgentName());
        singleObject.setPhones(oneLGObject.getPhones());
        singleObject.setSalesAgentEmail(oneLGObject.getSalesAgentEmail());
        singleObject.setLatitude(oneLGObject.getLatitude());
        singleObject.setLongitude(oneLGObject.getLongitude());

        String bathroom = "to be confirmed";
        if (oneLGObject.getBathroomUnit().equals("раздельный + совмещенный")) {
            bathroom = "роздільний + суміщений";
        } else if (oneLGObject.getBathroomUnit().equals("совмещенный")) {
            bathroom = "суміщений";
        } else if (oneLGObject.getBathroomUnit().equals("раздельный")) {
            bathroom = "роздільний";
        }

        singleObject.setBathroomUnit(bathroom);
        singleObject.setId(oneRiaLGObject.getLocalRealtyId());
        singleObject.setImages(oneRiaLGObject.getLoc());
        singleObject.setStreet(oneRiaLGObject.getStreet());
        singleObject.setBuildingNumber(oneRiaLGObject.getBuildingNumber());
        singleObject.setDistrict(oneRiaLGObject.getDistrict());
        singleObject.setMetro(oneRiaLGObject.getMetro());
        singleObject.setTotalArea(oneRiaLGObject.getTotalArea());
        singleObject.setRooms(oneRiaLGObject.getRoomsCount());
        singleObject.setFloor(oneRiaLGObject.getFloor());
        singleObject.setFloors(oneRiaLGObject.getFloors());
        singleObject.setOfferType(oneRiaLGObject.getOfferType());
        singleObject.setDescription(oneRiaLGObject.getDescription());
        singleObject.setCurrency(oneRiaLGObject.getCurrency());
        singleObject.setPrice(oneRiaLGObject.getPrice());

        String json = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(singleObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(json);
    }

    @GetMapping("/{type}/{category}")
    public ResponseEntity<?> getObjects(
            @PathVariable String type,
            @PathVariable String category,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String rooms,
            @RequestParam(required = false) String newBuildingName,
            @RequestParam(required = false) String metro,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String fromArea,
            @RequestParam(required = false) String toArea,
            @RequestParam(required = false) String fromPrice,
            @RequestParam(required = false) String toPrice,
            @RequestParam(required = false) String fromKitchenArea,
            @RequestParam(required = false) String toKitchenArea,
            @RequestParam(required = false) String offerType,
            @RequestParam(required = false) String street,
            @RequestParam(required = false, defaultValue = "0") Integer page
    ) {

        if (page < 0) page = 0;

        String typeInXML = "";
        List<String> categoryInXML = new ArrayList<>();

        if (type.equals("buy")) {
            typeInXML = "продаж";
        } else if (type.equals("rent")) {
            typeInXML = "довгострокова оренда";
        }

        if (category.equals("houses")) {
            categoryInXML.add("будинок");
            categoryInXML.add("таунхаус");
        } else if (category.equals("apartments")) {
            categoryInXML.add("квартира");
            categoryInXML.add("кімната");
        } else if (category.equals("land")) {
            categoryInXML.add("ділянка під житлову забудову");
            categoryInXML.add("земля комерційного призначення");
            categoryInXML.add("земля сільськогосподарського призначення");
        } else if (category.equals("offices")) {
            categoryInXML.add("офісне приміщення");
            categoryInXML.add("кафе, бар, ресторан");
            categoryInXML.add("об'єкт сфери послуг");
            categoryInXML.add("торгові площі");
            categoryInXML.add("приміщення вільного призначення");
        }

        Pageable pageable = PageRequest.of(page, ITEMS_PER_PAGE, Sort.by(Sort.Direction.ASC, "currency").and(Sort.by(Sort.Direction.DESC, "price")));

        Page<RiaLGObject> objectsPage = riaLGObjectService.getObjectsByFilters(
                typeInXML, categoryInXML,
                (!district.isEmpty() ? Arrays.asList(district.split("\\s*,\\s*")) : null),
                (!newBuildingName.isEmpty() ? Arrays.asList(newBuildingName.split("\\s*,\\s*")) : null),
                (isNumeric(fromArea) ? Double.parseDouble(fromArea) : 0.0),
                (isNumeric(toArea) ? Double.parseDouble(toArea) : Double.MAX_VALUE),
                (isNumeric(fromPrice) ? Double.parseDouble(fromPrice) : 0.0),
                (isNumeric(toPrice) ? Double.parseDouble(toPrice) : Double.MAX_VALUE),
                (!rooms.isEmpty() ? Arrays.asList(rooms.split("\\s*,\\s*")) : null),
                street,
                (!metro.isEmpty() ? Arrays.asList(metro.split("\\s*,\\s*")) : null),
                (!floor.isEmpty() ? Arrays.asList(floor.split("\\s*,\\s*")) : null),
                (isNumeric(fromKitchenArea) ? Double.parseDouble(fromKitchenArea) : 0.0),
                (isNumeric(toKitchenArea) ? Double.parseDouble(toKitchenArea) : Double.MAX_VALUE),
                offerType, pageable);

        List<RiaLGObject> allObjects = objectsPage.getContent();

        List<String> temp = new ArrayList<>();
        for (RiaLGObject riaLGObject : allObjects) {
                temp.add(makeJson(riaLGObject));
        }

        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for (String jsonString : temp) {
            jsonArray.add(gson.fromJson(jsonString, JsonObject.class));
        }
        String data = jsonArray.toString();


        long pageCount = objectsPage.getTotalPages();


        List<RiaLGObject> objectsForFilters = riaLGObjectService.getDataForFilters(
                typeInXML, categoryInXML,
                (!district.isEmpty() ? Arrays.asList(district.split("\\s*,\\s*")) : null),
                (!newBuildingName.isEmpty() ? Arrays.asList(newBuildingName.split("\\s*,\\s*")) : null),
                (isNumeric(fromArea) ? Double.parseDouble(fromArea) : 0.0),
                (isNumeric(toArea) ? Double.parseDouble(toArea) : Double.MAX_VALUE),
                (isNumeric(fromPrice) ? Double.parseDouble(fromPrice) : 0.0),
                (isNumeric(toPrice) ? Double.parseDouble(toPrice) : Double.MAX_VALUE),
                (!rooms.isEmpty() ? Arrays.asList(rooms.split("\\s*,\\s*")) : null),
                street,
                (!metro.isEmpty() ? Arrays.asList(metro.split("\\s*,\\s*")) : null),
                (!floor.isEmpty() ? Arrays.asList(floor.split("\\s*,\\s*")) : null),
                (isNumeric(fromKitchenArea) ? Double.parseDouble(fromKitchenArea) : 0.0),
                (isNumeric(toKitchenArea) ? Double.parseDouble(toKitchenArea) : Double.MAX_VALUE),
                offerType);

        List<String> tempDataForFilters = new ArrayList<>();

        for (RiaLGObject riaLGObject : objectsForFilters) {
            tempDataForFilters.add(makeJsonForFilters(riaLGObject));
        }

        Gson gsonForFilters = new Gson();
        JsonArray jsonArrayForFilters  = new JsonArray();
        for (String jsonString : tempDataForFilters) {
            jsonArrayForFilters.add(gsonForFilters .fromJson(jsonString, JsonObject.class));
        }

        String dataForFilters = jsonArrayForFilters.toString();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("totalPages", pageCount);
        responseMap.put("data", data);
        responseMap.put("dataForFilters", dataForFilters);

        return ResponseEntity.ok(responseMap);

    }

    @GetMapping("/{type}")
    public ResponseEntity<?> getBuy(@PathVariable String type) {

        List<String> housesList = new ArrayList<>();
        List<String> apartmentsList = new ArrayList<>();
        List<String> landList = new ArrayList<>();
        List<String> officesList = new ArrayList<>();

        if (type.equals("buy")) {
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/8492a_30a59697a2a2db763f1f8fbe66966b1c.jpg");
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/dd4c2_42bff16cfa2b0f778844ecb0fd9e8a15.jpg");
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/df1f2_e8c7777f743abf1156cd9aada1dc170e.jpg");
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/a5b07_bce96927f1421da44b94c3756b751f8d.jpg");
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/20da5_b6de0870d6afbfd4c66671c08d0ae547.jpg");
            housesList.add("http://120064.ligapro.ua/pic/gallery/sell_house/sd6687/ee344_0711a731c2f2a3ac8a5aa7228417d476.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/sell_app/sd6687/6e663_409d399b54c3d619d66c33e5e8a037f6.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/sell_app/sd6687/c19c1_7c5da2939fe85866c6ed2d947a6002b3.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/sell_app/sd6687/86914_196c20eb190d4f876de7efd8b9fd05c6.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/sell_app/sd6687/2367a_6274d3164a2a2847d8acbdd589beec3.webp");
            landList.add("http://120064.ligapro.ua/pic/gallery/sell_zu/sd6687/19ae6_f2685518cd9ade63531403dcb319d190.jpg");
            landList.add("http://120064.ligapro.ua/pic/gallery/sell_zu/sd6687/a7cb968b19ac6c16917700594fb93282.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/sell_nf/sd6687/2a418_abf0617f0b05b20bc30f7cc57aede19a.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/sell_komm/sd6687/418c6e40d8be586548d347b3c82d7883.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/sell_komm/sd6687/fd183_522caee2696d49030fc650810fe01a42.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/sell_komm/sd6687/20820_bdd9c9b8fb86a597abc00e445d744870.jpg");
        } else if (type.equals("rent")) {
            housesList.add("http://120064.ligapro.ua/pic/gallery/rent_house/sd6687/42079_5ae69f13eb590428477ef4f945975af8.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/rent_app/sd6687/68cf7_bd0ccd9359c2cef1e0a56ef2cab9a12c.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/rent_app/sd6687/f144a_6685e01912282b005c8863e512f05a8a.jpg");
            apartmentsList.add("http://120064.ligapro.ua/pic/gallery/rent_app/sd6687/c00d6_2bdbf09c26fe2f398f1b85efea90201f.jpg");
            landList.add("http://120064.ligapro.ua/pic/gallery/sell_zu/sd6687/c48c7_94fc4dfab6cba58a9d13d4a8c61e9934.png");
            officesList.add("http://120064.ligapro.ua/pic/gallery/rent_office/sd6687/d657c_e52d3f10b7cacfbef9aa5822a076f36b.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/rent_komm/sd6687/15523de28d84b2e614b9c8360f7e1557.jpg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/rent_komm/sd6687/7b42d_079873b365a26cc70b141922030d3bb.jpeg");
            officesList.add("http://120064.ligapro.ua/pic/gallery/rent_komm/sd6687/425a6_ccab053b181f1deaa9887ff86e6293a.webp");
        }

        Random random = new Random();

        int randomHousesIndex = random.nextInt(housesList.size());
        String houses = housesList.get(randomHousesIndex);

        int randomApartmentsIndex = random.nextInt(apartmentsList.size());
        String apartments = apartmentsList.get(randomApartmentsIndex);

        int randomLandIndex = random.nextInt(landList.size());
        String land = landList.get(randomLandIndex);

        int randomOfficesIndex = random.nextInt(officesList.size());
        String offices = officesList.get(randomOfficesIndex);

        String imgs = "{\n" +
                "\"houses\": \"" + houses + "\",\n" +
                "\"apartments\": \"" + apartments + "\",\n" +
                "\"land\": \"" + land + "\",\n" +
                "\"offices\": \"" + offices + "\"\n" +
                "}";

        return ResponseEntity.ok(imgs);
    }

    @GetMapping("/test")
    public List<String> getBuy() {

        List<String> names = new ArrayList<>();

        List<LGObject> allObjects = objectService.get();
        List<RiaLGObject> riaLGObjects = riaLGObjectService.get();

        for (LGObject lgObject : allObjects) {
            if (!names.contains(lgObject.getSalesAgentName())) {
                names.add(lgObject.getSalesAgentName());
            }
        }
        names.add("_________________________________");

        for (RiaLGObject riaLGObject : riaLGObjects) {
            if (!names.contains(riaLGObject.getRealtyType())) {
                names.add(riaLGObject.getRealtyType());
            }
        }

        names.add("_________________________________");

        for (RiaLGObject riaLGObject : riaLGObjects) {
            if (!names.contains(riaLGObject.getAdvertType())) {
                names.add(riaLGObject.getAdvertType());
            }
        }

        return names;
    }

    private static String makeJson(RiaLGObject riaLGObject) {

        String img = "to be confirmed";
        if (riaLGObject.getLoc().length() > 0) {
            String [] imgArr = riaLGObject.getLoc().split(",");
            img = imgArr[0];
        }

        return "{\n" +
                "\"id\": \"" + riaLGObject.getLocalRealtyId() + "\",\n" +
                "\"image\": \"" + img + "\",\n" +
                "\"street\": \"" + riaLGObject.getStreet() + "\",\n" +
                "\"buildingNumber\": \"" + riaLGObject.getBuildingNumber() + "\",\n" +
                "\"district\": \"" + riaLGObject.getDistrict() + "\",\n" +
                "\"metro\": \"" + riaLGObject.getMetro() + "\",\n" +
                "\"totalArea\": \"" + riaLGObject.getTotalArea() + "\",\n" +
                "\"kitchenArea\": \"" + riaLGObject.getKitchenArea() + "\",\n" +
                "\"rooms\": \"" + riaLGObject.getRoomsCount() + "\",\n" +
                "\"floor\": \"" + riaLGObject.getFloor().trim() + "\",\n" +
                "\"floors\": \"" + riaLGObject.getFloors().trim() + "\",\n" +
                "\"offerType\": \"" + riaLGObject.getOfferType().trim() + "\",\n" +
                "\"newBuildingName\": \"" + riaLGObject.getNewBuildingName() + "\",\n" +
                "\"currency\": \"" + riaLGObject.getCurrency() + "\",\n" +
                "\"price\": \"" + riaLGObject.getPrice() + "\"\n" +
                "}";
    }

    private static String makeJsonForFilters(RiaLGObject riaLGObject) {
        return "{\n" +
                "\"street\": \"" + riaLGObject.getStreet() + "\",\n" +
                "\"district\": \"" + riaLGObject.getDistrict() + "\",\n" +
                "\"metro\": \"" + riaLGObject.getMetro() + "\",\n" +
                "\"rooms\": \"" + riaLGObject.getRoomsCount() + "\",\n" +
                "\"floor\": \"" + riaLGObject.getFloor().trim() + "\",\n" +
                "\"offerType\": \"" + riaLGObject.getOfferType().trim() + "\",\n" +
                "\"newBuildingName\": \"" + riaLGObject.getNewBuildingName() + "\"\n" +
                "}";
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}