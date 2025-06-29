package org.example.controller;

import org.example.model.Athlete;
import org.example.model.Coach;
import org.example.repository.AthleteRepository;
import org.example.service.CoachUserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/athletes")
public class AthleteController {

    private final AthleteRepository athleteRepo;
    private final CoachUserDetailsService coachService;

    public AthleteController(AthleteRepository athleteRepo,
                             CoachUserDetailsService coachService) {
        this.athleteRepo  = athleteRepo;
        this.coachService = coachService;
    }

    /**
     * Redirect to dashboard for athlete list
     * GET /athletes
     */
    @GetMapping
    public String listAndForm() {
        return "redirect:/";
    }

    /**
     * Show the "edit athlete" form.
     * GET /athletes/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                           Principal principal) {

        Athlete a = athleteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid athlete ID:" + id));

        // guard: only this coach may edit
        if (!a.getCoach().getUsername().equals(principal.getName())) {
            throw new IllegalArgumentException("Not your athlete");
        }

        model.addAttribute("athlete", a);
        return "athlete_edit";   // renders templates/athlete_edit.html
    }

    /**
     * Handle the edit form submission.
     * POST /athletes/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("athlete") Athlete formAthlete,
                         Principal principal) {

        Athlete existing = athleteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid athlete ID:" + id));

        // guard again
        if (!existing.getCoach().getUsername().equals(principal.getName())) {
            throw new IllegalArgumentException("Not your athlete");
        }

        // apply changes
        existing.setName(formAthlete.getName());
        existing.setWeight(formAthlete.getWeight());
        existing.setSize(formAthlete.getSize());

        athleteRepo.save(existing);
        return "redirect:/?athleteId=" + existing.getId();
    }

    /**
     * Delete an athlete.
     * POST /athletes/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        Athlete existing = athleteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid athlete ID:" + id));

        if (!existing.getCoach().getUsername().equals(principal.getName())) {
            throw new IllegalArgumentException("Not your athlete");
        }

        athleteRepo.delete(existing);
        return "redirect:/";
    }
}
