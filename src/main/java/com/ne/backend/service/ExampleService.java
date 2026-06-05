package com.ne.backend.service;

import com.ne.backend.entity.ExampleEntity;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.ExampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleService {

    private final ExampleRepository exampleRepository;

    public Page<ExampleEntity> getAll(Pageable pageable) {
        log.info("Fetching all examples with pagination");
        return exampleRepository.findAll(pageable);
    }

    public Page<ExampleEntity> searchExamples(String name, String description, Pageable pageable) {
        log.info("Searching examples with filters - name: {}, description: {}", name, description);
        return exampleRepository.searchExamples(name, description, pageable);
    }

    public ExampleEntity getById(Long id) {
        log.info("Fetching example by ID: {}", id);

        return exampleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Example not found"));
    }

    public ExampleEntity create(ExampleEntity entity) {
        log.info("Creating new example with name: {}", entity.getName());
        ExampleEntity saved = exampleRepository.save(entity);
        log.info("Example created successfully with ID: {}", saved.getId());
        return saved;
    }

    public ExampleEntity update(Long id, ExampleEntity request) {
        log.info("Updating example with ID: {}", id);

        ExampleEntity entity = getById(id);

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        ExampleEntity updated = exampleRepository.save(entity);
        log.info("Example updated successfully with ID: {}", id);
        return updated;
    }

    public void delete(Long id) {
        log.info("Deleting example with ID: {}", id);

        ExampleEntity entity = getById(id);

        exampleRepository.delete(entity);

        log.info("Example deleted successfully with ID: {}", id);
    }
}