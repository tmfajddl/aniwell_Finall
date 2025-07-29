package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.repository.PetVaccinationRepository;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetVaccination;
import com.example.RSW.vo.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PetNotificationScheduler {

    @Autowired
    Rq rq;

    private final PetRepository petRepository;
    private final PetVaccinationRepository vaccinationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 11 * * *") // 매일 오전 8시
    public void run() {
        sendBirthdayNotifications();
        sendVaccineNotifications();
    }

    private void sendBirthdayNotifications() {
        List<Pet> upcomingBirthdays = petRepository.findPetsWithBirthdayInDays(List.of(0, 3, 7));
        for (Pet pet : upcomingBirthdays) {
            String title = "🎉 " + pet.getName() + "의 생일이 " + getDday(pet.getBirthDate()) + "일 남았어요!";
            String link = "/usr/pet/list";
            int loginMemberId = rq.getLoginedMemberId();
            int petId = pet.getId();
            notificationService.addNotification(loginMemberId, petId, "birthday", title, link);
        }
    }

    private void sendVaccineNotifications() {
        List<PetVaccination> dueVaccines = vaccinationRepository.findNextDueInDays(List.of(0, 3, 7));
        for (PetVaccination vac : dueVaccines) {
            String title = "💉 " + vac.getPetName() + "의 " + vac.getVaccineName() + " 백신 접종일이 " +
                    getDday(vac.getNextDueDate()) + "일 남았어요!";
            String link = "/usr/pet/petPage";
            int loginMemberId = rq.getLoginedMemberId();
            int petId = vac.getPetId();
            notificationService.addNotification(loginMemberId, petId, "vaccine", title, link);
        }
    }

    private int getDday(Date date) {
        long diff = date.getTime() - System.currentTimeMillis();
        return (int)(diff / (1000 * 60 * 60 * 24));
    }
}
