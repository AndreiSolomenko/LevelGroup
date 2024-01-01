package com.levelgroup;

import com.levelgroup.model.LGObject;
import com.levelgroup.model.RiaLGObject;
import com.levelgroup.services.LGObjectService;
import com.levelgroup.services.RiaLGObjectService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.net.URLConnection;
import java.io.InputStream;
import java.net.URL;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG =
            LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    private final LGObjectService lgObjectService;
    private final RiaLGObjectService riaLgObjectService;

    public CommandLineAppStartupRunner(LGObjectService lgObjectService, RiaLGObjectService riaLgObjectService) {
        this.lgObjectService = lgObjectService;
        this.riaLgObjectService = riaLgObjectService;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Application started");
        parseXmlFiles();
    }

    @Scheduled(fixedRate = 43200000)
    public void runXmlParsingScheduled() {
        LOG.info("Scheduled XML Parsing");
        parseXmlFiles();
    }

    private void parseXmlFiles() {
        try {
            parseFirstXmlFile("http://120064.ligapro.ua/files/yandex_xml/ya_xml_base_cdf28_0ea7f.xml");
            parseSecondXmlFile("http://120064.ligapro.ua/files/yandex_xml/ria_ukr_xml_cdf28_0ea7f.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseFirstXmlFile(String fileUrl) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

            try (InputStream inputStream = urlConnection.getInputStream()) {
                Document document = builder.parse(inputStream);

                Element root = document.getDocumentElement();
                NodeList offerList = root.getElementsByTagName("offer");

                for (int i = 0; i < offerList.getLength(); i++) {
                    Element offerElement = (Element) offerList.item(i);

                    LGObject lgObject = new LGObject();

                    Element locationElement = (Element) offerElement.getElementsByTagName("location").item(0);
//                    Element metroElement = (Element) locationElement.getElementsByTagName("metro").item(0);
                    Element salesAgentElement = (Element) offerElement.getElementsByTagName("sales-agent").item(0);
//                    Element priceElement = (Element) offerElement.getElementsByTagName("price").item(0);
//                    Element areaElement = (Element) offerElement.getElementsByTagName("area").item(0);
//                    Element livingSpaceElement = (Element) offerElement.getElementsByTagName("living-space").item(0);
//                    Element kitchenSpaceElement = (Element) offerElement.getElementsByTagName("kitchen-space").item(0);

                    lgObject.setInternalId(offerElement.getAttribute("internal-id"));
//                    lgObject.setType(getTextValue(offerElement, "type"));
//                    lgObject.setPropertyType(getTextValue(offerElement, "property-type"));
//                    lgObject.setCategory(getTextValue(offerElement, "category"));
//                    lgObject.setUrl(getTextValue(offerElement, "url"));
//                    lgObject.setCreationDate(getTextValue(offerElement, "creation-date"));
//                    lgObject.setLastUpdateDate(getTextValue(offerElement, "last-update-date"));
//                    lgObject.setCountry(getTextValue(locationElement, "country"));
//                    lgObject.setRegion(getTextValue(locationElement, "region"));
//                    lgObject.setLocalityName(getTextValue(locationElement, "locality-name"));
//                    lgObject.setSubLocalityName(getTextValue(locationElement, "sub-locality-name"));
//                    lgObject.setMetroName(getTextValue(metroElement, "name"));
//                    lgObject.setAddress(getTextValue(locationElement, "address"));
                    lgObject.setLatitude(getTextValue(locationElement, "latitude"));
                    lgObject.setLongitude(getTextValue(locationElement, "longitude"));
                    lgObject.setSalesAgentName(getTextValue(salesAgentElement, "name"));
                    lgObject.setPhones(getTextValues(salesAgentElement, "phone"));
                    lgObject.setSalesAgentEmail(getTextValue(salesAgentElement, "email"));
//                    lgObject.setSalesAgentCategory(getTextValue(salesAgentElement, "category"));
//                    lgObject.setSalesAgentUrl(getTextValue(salesAgentElement, "url"));
//                    lgObject.setPriceValue(getTextValue(priceElement, "value"));
//                    lgObject.setPriceCurrency(getTextValue(priceElement, "currency"));
//                    lgObject.setPricePeriod(getTextValue(priceElement, "period"));
//                    lgObject.setImages(getTextValues(offerElement, "image"));
//                    lgObject.setAreaValue(getTextValue(areaElement, "value"));
//                    lgObject.setAreaUnit(getTextValue(areaElement, "unit"));
//                    lgObject.setLivingSpaceValue(getTextValue(livingSpaceElement, "value"));
//                    lgObject.setLivingSpaceUnit(getTextValue(livingSpaceElement, "unit"));
//                    lgObject.setKitchenSpaceValue(getTextValue(kitchenSpaceElement, "value"));
//                    lgObject.setKitchenSpaceUnit(getTextValue(kitchenSpaceElement, "unit"));
//                    lgObject.setRooms(getTextValue(offerElement, "rooms"));
//                    lgObject.setRoomsType(getTextValue(offerElement, "rooms-type"));
//                    lgObject.setFloor(getTextValue(offerElement, "floor"));
//                    lgObject.setFloorsTotal(getTextValue(offerElement, "floors-total"));
//                    lgObject.setLift(getTextValue(offerElement, "lift"));
//                    lgObject.setBalcony(getTextValue(offerElement, "balcony"));
                    lgObject.setBathroomUnit(getTextValue(offerElement, "bathroom-unit"));
//                    lgObject.setInternet(getTextValue(offerElement, "internet"));
//                    lgObject.setRoomFurniture(getTextValue(offerElement, "room-furniture"));
//                    lgObject.setTelevision(getTextValue(offerElement, "television"));
//                    lgObject.setRefrigerator(getTextValue(offerElement, "refrigerator"));
//                    lgObject.setNewFlat(getTextValue(offerElement, "new-flat"));
//                    lgObject.setTitle(getTextValue(offerElement, "title"));
//                    lgObject.setDescription(getTextValue(offerElement, "description"));

                    lgObjectService.add(lgObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseSecondXmlFile(String fileUrl) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

            try (InputStream inputStream = urlConnection.getInputStream()) {
                Document document = builder.parse(inputStream);

                Element root = document.getDocumentElement();
                NodeList realtyList = root.getElementsByTagName("realty");

                for (int i = 0; i < realtyList.getLength(); i++) {
                    Element realtyElement = (Element) realtyList.item(i);

                    RiaLGObject riaLGObject = new RiaLGObject();

                    Element characteristicsElement = (Element) realtyElement.getElementsByTagName("characteristics").item(0);
                    Element photosUrlsElement = (Element) realtyElement.getElementsByTagName("photos_urls").item(0);

                    riaLGObject.setLocalRealtyId(getTextValueForRia(realtyElement, "local_realty_id"));
                    riaLGObject.setRealtyType(getTextValueForRia(realtyElement, "realty_type"));
                    riaLGObject.setAdvertType(getTextValueForRia(realtyElement, "advert_type"));
                    riaLGObject.setDistrict(getTextValueForRia(realtyElement, "district"));
                    riaLGObject.setMetro(getTextValueForRia(realtyElement, "metro"));
                    riaLGObject.setStreet(getTextValueForRia(realtyElement, "street"));
                    riaLGObject.setBuildingNumber(getTextValueForRia(realtyElement, "building_number"));
                    riaLGObject.setNewBuildingName(getTextValueForRia(realtyElement, "newbuilding_name"));
                    riaLGObject.setRoomsCount(getTextValueForRia(characteristicsElement, "rooms_count"));

                    String totalAreaText = getTextValueForRia(characteristicsElement, "total_area");
                    Double totalArea = null;
                    try {
                        totalArea = Double.parseDouble(totalAreaText);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    riaLGObject.setTotalArea(totalArea);

                    String kitchenAreaText = getTextValueForRia(characteristicsElement, "kitchen_area");
                    Double kitchenArea = null;
                    try {
                        kitchenArea = Double.parseDouble(kitchenAreaText);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    riaLGObject.setKitchenArea(kitchenArea);

                    riaLGObject.setPlotArea(getTextValueForRia(characteristicsElement, "plot_area"));
                    riaLGObject.setPlotAreaUnit(getTextValueForRia(characteristicsElement, "plot_area_unit"));
                    riaLGObject.setFloor(getTextValueForRia(characteristicsElement, "floor"));
                    riaLGObject.setFloors(getTextValueForRia(characteristicsElement, "floors"));

                    String priceText = getTextValueForRia(characteristicsElement, "price");
                    Double price = null;
                    try {
                        price = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    riaLGObject.setPrice(price);

                    riaLGObject.setCurrency(getTextValueForRia(characteristicsElement, "currency"));
                    riaLGObject.setOfferType(getTextValueForRia(characteristicsElement, "offer_type"));
                    riaLGObject.setDescription(getTextValueForRia(realtyElement, "description"));
                    riaLGObject.setLoc(getTextValues(photosUrlsElement, "loc"));

                    riaLgObjectService.add(riaLGObject);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTextValue(Element parentElement, String elementName) {
        if (parentElement != null) {
            NodeList nodeList = parentElement.getElementsByTagName(elementName);
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                String textContent = element.getTextContent();

                if (textContent != null && !textContent.trim().isEmpty()) {
                    return textContent;
                }
            }
        }

        switch (elementName) {
            case "bathroom-unit":
                return "to be confirmed";
            case "latitude":
            case "longitude":
                return "0";
            default:
                return "";
        }
    }

    private static List<String> getTextValues(Element parentElement, String elementName) {
        List<String> values = new ArrayList<>();

        if (parentElement != null) {
            NodeList nodeList = parentElement.getElementsByTagName(elementName);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    values.add(element.getTextContent());
                }
            }
        }
        return values;
    }

    private static String getTextValueForRia(Element parentElement, String elementName) {
        if (parentElement != null) {
            NodeList nodeList = parentElement.getElementsByTagName(elementName);
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                String textContent = element.getTextContent();

                if (textContent != null && !textContent.trim().isEmpty()) {
                    return textContent;
                }
            }
        }
        return  "to be confirmed";
    }

}




//import java.util.HashSet;
//import java.util.Set;
//import org.w3c.dom.Node;
//
//@Component
//public class CommandLineAppStartupRunner implements CommandLineRunner {
//    private static final Logger LOG = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);
//    @Override
//    public void run(String... args) throws Exception {
//        LOG.info("Print Unique XML Tag Names");
//        try {
//            printUniqueXmlTagNames("http://120064.ligapro.ua/files/yandex_xml/ya_xml_base_cdf28_0ea7f.xml");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    private void printUniqueXmlTagNames(String fileUrl) {
//        Set<String> uniqueTagNames = new HashSet<>();
//        try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//
//            URL url = new URL(fileUrl);
//            URLConnection urlConnection = url.openConnection();
//            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
//
//            try (InputStream inputStream = urlConnection.getInputStream()) {
//                Document document = builder.parse(inputStream);
//
//                addUniqueTagNames(document.getDocumentElement(), uniqueTagNames);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        uniqueTagNames.forEach(tagName -> System.out.println("Unique Tag Name: " + tagName));
//    }
//
//    private void addUniqueTagNames(Node node, Set<String> uniqueTagNames) {
//        if (node.getNodeType() == Node.ELEMENT_NODE) {
//            uniqueTagNames.add(node.getNodeName());
//        }
//
//        NodeList nodeList = node.getChildNodes();
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Node childNode = nodeList.item(i);
//            addUniqueTagNames(childNode, uniqueTagNames);
//        }
//    }
//}