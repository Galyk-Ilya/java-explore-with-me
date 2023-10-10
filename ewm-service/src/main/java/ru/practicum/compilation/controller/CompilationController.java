package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationController {
    private final CompilationService compilationService;

    @GetMapping
    List<CompilationDto> findCompilations(@RequestParam(required = false) boolean pinned,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "10") @Positive int size) {
        List<CompilationDto> compilation = compilationService.findCompilations(pinned, from, size);
        log.info("Getting a list of event collections.");
        return compilation;
    }

    @GetMapping("/{compId}")
    CompilationDto findCompilation(@PathVariable Long compId) {
        CompilationDto compilation = compilationService.findCompilation(compId);
        log.info("Getting a selection of events id = {}.", compId);
        return compilation;
    }
}