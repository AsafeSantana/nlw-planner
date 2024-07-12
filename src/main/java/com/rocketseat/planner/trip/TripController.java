package com.rocketseat.planner.trip;

import com.rocketseat.planner.activity.ActivityData;
import com.rocketseat.planner.activity.ActivityRequestPayload;
import com.rocketseat.planner.activity.ActivityResponse;
import com.rocketseat.planner.activity.ActivityService;
import com.rocketseat.planner.links.LinkData;
import com.rocketseat.planner.links.LinkRequestPayload;
import com.rocketseat.planner.links.LinkResponse;
import com.rocketseat.planner.links.LinkService;
import com.rocketseat.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private TripRepository repository;

    //cria uma viagem
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload){
    Trip newTrip = new Trip(payload);

    repository.save(newTrip);

    participantService.registerParticipantToEvent(payload.emails_to_invite(),newTrip);

    return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    //recupera detalhes da viagem
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip>  trip = this.repository.findById(id);

        return trip.map(ResponseEntity:: ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



    //altera uma viagem
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()){
            Trip realTrip = trip.get();
            realTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            realTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(),DateTimeFormatter.ISO_DATE_TIME));
            realTrip.setDestination(payload.destination());

            this.repository.save(realTrip);

            return ResponseEntity.ok(realTrip);

        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()){
            Trip realTrip = trip.get();
            realTrip.setIsConfirmed(true);

            this.repository.save(realTrip);
            this.participantService.triggerConfirmationEmailToPArticipants(id);

            return ResponseEntity.ok(realTrip);

        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);
        if (trip.isPresent()){
            Trip rawTrip = trip.get();


            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantResponse);
        }

        return  ResponseEntity.notFound().build();

    }


    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participantList);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);


        if (trip.isPresent()){
            Trip rawTrip = trip.get();


            ActivityResponse activityResponse = this.activityService.registerActivity(payload,rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return  ResponseEntity.notFound().build();


    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id){
        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(id);

        return ResponseEntity.ok(activityDataList);
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {

        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();


            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);


            return ResponseEntity.ok(linkResponse);
        }
        return ResponseEntity.notFound().build();

    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id){
        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkDataList);
    }

}
