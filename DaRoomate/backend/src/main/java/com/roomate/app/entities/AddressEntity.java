package com.roomate.app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    // TODO: Integrate Google Places Autocomplete on the frontend to auto-fill these fields.
    // All new fields (province, postal, country) should be nullable to maintain backwards compatibility.
    // Also update: RoomEntity (add nullable city/province/postalCode/country columns),
    //              CreateRoomRequest DTO, and RoomDto to carry structured address data.
    private String street;
    private String city;
    private String province;
    private String postal;
    private String country;


}
