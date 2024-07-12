package com.rocketseat.planner.participant;

import com.rocketseat.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository repository;
    public void registerParticipantToEvent(List<String>participantsToInvite, Trip tripId){
      List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email,tripId)).toList();

      this.repository.saveAll(participants);

        System.out.println(participants.get(0).getId());

    }

        public ParticipantCreateResponse registerParticipantToEvent(String email, Trip trip){
        Participant newParticipant = new Participant(email,trip);
        this.repository.save(newParticipant);
        return  new ParticipantCreateResponse(newParticipant.getId());
        }

        public void triggerConfirmationEmailToPArticipants(UUID tripId){

        }

        public void triggerConfirmationEmailToParticipant(String email){}

        public List<ParticipantData> getAllParticipantsFromEvent(UUID tripId){
        return this.repository.findByTripId(tripId).stream().map(participant -> new ParticipantData(participant.getId(), participant.getName(), participant.getEmail(), participant.getIsConfirmed())).toList();
        }

}
