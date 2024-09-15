package com.levelgroup.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name="LGObject")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class LGObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String internalId;
    private String latitude;
    private String longitude;
    private String salesAgentName;


    private String phones;

    private String salesAgentEmail;
    private String bathroomUnit;

}