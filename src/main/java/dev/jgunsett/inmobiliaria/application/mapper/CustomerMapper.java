package dev.jgunsett.inmobiliaria.application.mapper;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerResponse;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;

@Component
public class CustomerMapper {

    /**
     * CreateRequest → Entity
     */
    public Customer toEntity(CustomerCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Customer.builder()
                .name(request.getName())
                .middleName(request.getMiddleName())
                .surname(request.getSurname())
                .secondSurname(request.getSecondSurname())
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .cuit(request.getCuit())
                .birthdate(request.getBirthdate())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    /**
     * UpdateRequest → Entity (merge)
     */
    public void updateEntity(Customer customer, CustomerUpdateRequest request) {
        if (customer == null || request == null) {
            return;
        }

        customer.setName(request.getName());
        customer.setMiddleName(request.getMiddleName());
        customer.setSurname(request.getSurname());
        customer.setSecondSurname(request.getSecondSurname());
        customer.setDocumentType(request.getDocumentType());
        customer.setDocumentNumber(request.getDocumentNumber());
        customer.setCuit(request.getCuit());
        customer.setBirthdate(request.getBirthdate());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
    }

    /**
     * Entity → Response
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .middleName(customer.getMiddleName())
                .surname(customer.getSurname())
                .secondSurname(customer.getSecondSurname())
                .fullName(customer.getFullName())
                .documentType(customer.getDocumentType())
                .documentNumber(customer.getDocumentNumber())
                .cuit(customer.getCuit())
                .birthdate(customer.getBirthdate())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .creationDate(customer.getCreationDate())
                .modificationDate(customer.getModificationDate())
                .build();
    }
}
