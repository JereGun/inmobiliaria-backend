package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.IllegalStateException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ContractRepository contractRepository;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void createRejectsWhenOwnerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(createRequest(), null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("dueño");

        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createSavesPropertyWhenOwnerExists() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(propertyRepository.save(any())).thenReturn(property());

        service().create(createRequest(), null, null);

        verify(propertyRepository).save(any(Property.class));
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void updateRejectsWhenCoveredAreaExceedsTotalArea() {
        Property property = property();
        property.setTotalArea(100.0);
        property.setCoveredArea(80.0);
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setCoveredArea(120.0); // > totalArea 100

        assertThatThrownBy(() -> service().update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cubierta");
    }

    @Test
    void updateRejectsNegativeSalePrice() {
        Property property = property();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setSalePrice(-1.0);

        assertThatThrownBy(() -> service().update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("venta");
    }

    @Test
    void updateRejectsNegativeRentPrice() {
        Property property = property();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setRentPrice(0.0);

        assertThatThrownBy(() -> service().update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("alquiler");
    }

    @Test
    void updateRejectsOperationTypeChangeWhenActiveContractExists() {
        Property property = propertyWithRentPrice();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(contractRepository.existsByPropertyIdAndStatus(any(), any())).thenReturn(true);

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setOperationTypes(Set.of(OperationType.SALE)); // different from RENT

        assertThatThrownBy(() -> service().update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("operacion");
    }

    @Test
    void updateRejectsInvalidCoverImageId() {
        Property property = propertyWithRentPrice();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setCoverImageId(999L); // no such image

        assertThatThrownBy(() -> service().update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cover");
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void deleteRejectsWhenPropertyNotFound() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository, never()).delete(any());
    }

    @Test
    void deleteRejectsPropertyWithActiveContract() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property()));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service().delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("contrato");

        verify(propertyRepository, never()).delete(any());
    }

    @Test
    void deleteSucceedsWhenNoActiveContract() {
        Property property = property();
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);

        service().delete(1L);

        verify(propertyRepository).delete(property);
    }

    // -------------------------------------------------------------------------
    // search()
    // -------------------------------------------------------------------------

    @Test
    void searchRejectsBlankQuery() {
        assertThatThrownBy(() -> service().search("  ", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("búsqueda");
    }

    @Test
    void searchRejectsNullQuery() {
        assertThatThrownBy(() -> service().search(null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("búsqueda");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PropertyService service() {
        return new PropertyService(propertyRepository, customerRepository, contractRepository);
    }

    private Customer owner() {
        return Customer.builder().id(1L).name("Laura").surname("Gomez").build();
    }

    private Property property() {
        Property p = Property.builder()
                .id(1L)
                .name("Depto Centro")
                .owner(owner())
                .status(PropertyStatus.AVAILABLE)
                .propertyType(PropertyType.APARTMENT)
                .operationTypes(Set.of(OperationType.RENT))
                .build();
        p.setImages(new ArrayList<>());
        return p;
    }

    private Property propertyWithRentPrice() {
        Property p = propertyWithRentPrice(50000.0);
        return p;
    }

    private Property propertyWithRentPrice(double rentPrice) {
        Property p = Property.builder()
                .id(1L)
                .name("Depto Centro")
                .owner(owner())
                .status(PropertyStatus.AVAILABLE)
                .propertyType(PropertyType.APARTMENT)
                .operationTypes(Set.of(OperationType.RENT))
                .rentPrice(rentPrice)
                .build();
        p.setImages(new ArrayList<>());
        return p;
    }

    private PropertyCreateRequest createRequest() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setOwnerId(1L);
        req.setName("Depto Centro");
        req.setPropertyType(PropertyType.APARTMENT);
        req.setOperationTypes(Set.of(OperationType.RENT));
        req.setRentPrice(50000.0);
        return req;
    }
}
