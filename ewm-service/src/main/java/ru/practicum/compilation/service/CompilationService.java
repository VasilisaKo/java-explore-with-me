package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventPublicService;
import ru.practicum.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;

    private final EventPublicService eventPublicService;

    public List<CompilationDto> getAllCompilation(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findByPinned(pinned, pageable);
        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilations = compilationRepository.findById(compId).orElseThrow(() -> new ObjectNotFoundException("Compilation with id=" + compId + " was not found"));
        return CompilationMapper.toDto(compilations);
    }

    @Transactional
    public CompilationDto postCompilation(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto);
        if (dto.getEvents() == null) {
            compilation.setEvents(eventPublicService.findAll());
        } else {
            List<Event> events = eventPublicService.findEventsById(dto.getEvents());
            compilation.setEvents(events);
        }
        if (dto.getPinned() == null) {
            compilation.setPinned(false);
        }
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.toDto(compilation);
    }

    @Transactional
    public void deleteCompilation(Long id) {
        compilationRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Compilation with id=" + id + " was not found"));
        compilationRepository.deleteById(id);
    }

    public CompilationDto patchCompilation(UpdateCompilationRequest dto, Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new ObjectNotFoundException("Compilation with id=" + compId + " was not found"));

        if (dto.getEvents() == null) {
            dto.setEvents(eventPublicService.findAllIds());
        }
        compilation = updateCompilationAdmin(dto, compilation);
        return CompilationMapper.toDto(compilation);
    }

    @Transactional
    Compilation updateCompilationAdmin(UpdateCompilationRequest dto, Compilation compilation) {
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (!dto.getEvents().isEmpty() || dto.getEvents().size() != 0) {
            List<Event> events = eventPublicService.findEventsById(dto.getEvents());
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        return compilation;
    }
}
