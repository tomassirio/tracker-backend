package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.tomassirio.wanderer.command.service.impl.GoogleGeocodingServiceImpl;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleGeocodingServiceImplTest {

    private final GoogleGeocodingServiceImpl service =
            new GoogleGeocodingServiceImpl(null); // GeoApiContext not needed for unit tests

    @Test
    void reverseGeocode_whenLocationIsNull_shouldReturnNull() {
        // When
        GeocodingService.GeocodingResult result = service.reverseGeocode(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void reverseGeocode_whenLatIsNull_shouldReturnNull() {
        // Given
        GeoLocation location = GeoLocation.builder().lat(null).lon(2.3522).build();

        // When
        GeocodingService.GeocodingResult result = service.reverseGeocode(location);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void reverseGeocode_whenLonIsNull_shouldReturnNull() {
        // Given
        GeoLocation location = GeoLocation.builder().lat(48.8566).lon(null).build();

        // When
        GeocodingService.GeocodingResult result = service.reverseGeocode(location);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void extractCityAndCountry_whenLocalityAndCountryPresent_shouldReturnBoth() throws Exception {
        // Given
        AddressComponent locality = createComponent("Paris", AddressComponentType.LOCALITY);
        AddressComponent country = createComponent("France", AddressComponentType.COUNTRY);
        AddressComponent[] components = {locality, country};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Paris");
        assertThat(result.country()).isEqualTo("France");
    }

    @Test
    void extractCityAndCountry_whenNoLocality_shouldFallbackToAdminLevel2() throws Exception {
        // Given
        AddressComponent admin2 =
                createComponent("Île-de-France", AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2);
        AddressComponent country = createComponent("France", AddressComponentType.COUNTRY);
        AddressComponent[] components = {admin2, country};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Île-de-France");
        assertThat(result.country()).isEqualTo("France");
    }

    @Test
    void extractCityAndCountry_whenNoLocalityOrAdmin2_shouldFallbackToAdminLevel1()
            throws Exception {
        // Given
        AddressComponent admin1 =
                createComponent("Galicia", AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1);
        AddressComponent country = createComponent("Spain", AddressComponentType.COUNTRY);
        AddressComponent[] components = {admin1, country};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Galicia");
        assertThat(result.country()).isEqualTo("Spain");
    }

    @Test
    void extractCityAndCountry_whenLocalityPresentWithAdmin_shouldPreferLocality()
            throws Exception {
        // Given
        AddressComponent admin1 =
                createComponent("Galicia", AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1);
        AddressComponent locality =
                createComponent("Santiago de Compostela", AddressComponentType.LOCALITY);
        AddressComponent country = createComponent("Spain", AddressComponentType.COUNTRY);
        AddressComponent[] components = {admin1, locality, country};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Santiago de Compostela");
        assertThat(result.country()).isEqualTo("Spain");
    }

    @Test
    void extractCityAndCountry_whenOnlyCountry_shouldReturnCountryOnly() throws Exception {
        // Given
        AddressComponent country = createComponent("Spain", AddressComponentType.COUNTRY);
        AddressComponent[] components = {country};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isNull();
        assertThat(result.country()).isEqualTo("Spain");
    }

    @Test
    void extractCityAndCountry_whenEmptyComponents_shouldReturnNull() throws Exception {
        // Given
        AddressComponent[] components = {};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void extractCityAndCountry_whenNullComponents_shouldReturnNull() throws Exception {
        // When
        GeocodingService.GeocodingResult result = invokeExtract(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void extractCityAndCountry_whenNoRelevantTypes_shouldReturnNull() throws Exception {
        // Given
        AddressComponent postalCode = createComponent("75001", AddressComponentType.POSTAL_CODE);
        AddressComponent[] components = {postalCode};

        // When
        GeocodingService.GeocodingResult result = invokeExtract(components);

        // Then
        assertThat(result).isNull();
    }

    // --- helpers ---

    private AddressComponent createComponent(String longName, AddressComponentType... types) {
        AddressComponent c = new AddressComponent();
        c.longName = longName;
        c.types = types;
        return c;
    }

    /**
     * Invokes the private extractCityAndCountry method via reflection for isolated unit testing.
     */
    private GeocodingService.GeocodingResult invokeExtract(AddressComponent[] components)
            throws Exception {
        Method method =
                GoogleGeocodingServiceImpl.class.getDeclaredMethod(
                        "extractCityAndCountry", AddressComponent[].class);
        method.setAccessible(true);
        return (GeocodingService.GeocodingResult) method.invoke(service, (Object) components);
    }
}
