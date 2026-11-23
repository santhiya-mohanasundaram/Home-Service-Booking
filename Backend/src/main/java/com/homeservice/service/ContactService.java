package com.homeservice.service;

import com.homeservice.dto.ContactRequest;
import com.homeservice.entity.ContactMessage;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional
    public ContactMessage submitMessage(ContactRequest request) {
        ContactMessage msg = new ContactMessage(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getServiceType(),
                request.getSubject(),
                request.getMessage()
        );
        return contactMessageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact message not found with ID: " + id);
        }
        contactMessageRepository.deleteById(id);
    }
}
