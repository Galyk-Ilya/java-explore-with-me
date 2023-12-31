package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        var events = newCompilationDto.getEvents() == null ?
                new HashSet<Event>() : eventRepository.findAllById(newCompilationDto.getEvents());
        HashSet<Event> added = new HashSet<>();
        added.addAll(events);

        return CompilationMapper
                .toCompilationDto(compilationRepository.save(CompilationMapper
                        .toCompilation(newCompilationDto, added)));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, NewCompilationDto updateCompilationDto) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("No event selection data from id = " + compId + "."));

        var events = updateCompilationDto.getEvents() == null ?
                new HashSet<Event>() : eventRepository.findAllById(updateCompilationDto.getEvents());

        HashSet<Event> up = new HashSet<>();
        up.addAll(events);

        return CompilationMapper.toCompilationDto(compilationRepository.save(CompilationMapper
                .toCompilationForUpdate(compilation, updateCompilationDto, up)));
    }

    @Override
    public List<CompilationDto> findCompilations(boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream().map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilation(Long compId) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Collection of events not found id = " + compId + ".")));
    }
}