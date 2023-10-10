package ru.practicum.compilation.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.validation.ValidationGroup;

import javax.validation.Valid;

@RestController
@Slf4j
@AllArgsConstructor
@Validated
@RequestMapping("/admin/compilations")
public class AdministrationCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @Validated({ValidationGroup.AddCompilation.class})
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        CompilationDto compilation = compilationService.addCompilation(newCompilationDto);
        log.info("Admin adds a new selection of events");
        return compilation;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
        log.info("Admin deletes a selection of events id = {}", compId);
    }

    @PatchMapping("/{compId}")
    @Validated({ValidationGroup.UpdateCompilation.class})
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody @Valid NewCompilationDto updateCompilationDto) {
        CompilationDto compilation = compilationService.updateCompilation(compId, updateCompilationDto);
        log.info("The admin has made changes to the selection of events id = {}", compId);
        return compilation;
    }
}