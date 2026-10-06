package com.ruwalabs.saludya.iam.application.internal.eventhandlers;

import com.ruwalabs.saludya.iam.domain.repositories.UserAccountRepository;
import com.ruwalabs.saludya.iam.domain.services.NotificationGateway;
import com.ruwalabs.saludya.iam.domain.model.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.*;
@Component
public class IdentityEventHandlers {
    private static final Logger LOG=LoggerFactory.getLogger(IdentityEventHandlers.class);
    private final UserAccountRepository users;private final NotificationGateway notifications;
    public IdentityEventHandlers(UserAccountRepository users,NotificationGateway notifications) { this.users=users;this.notifications=notifications; }
    private void notify(Long id,String subject,String message) {
        users.findById(id).ifPresent(u->notifications.enqueue(u.getEmail().value(),subject,message));
    }
    @EventListener public void registered(PatientRegisteredEvent e) {
        notify(e.userId(),"Bienvenido a SaludYa","Tu correo fue verificado y tu cuenta de paciente está lista.");
        LOG.info("IAM patient registered: userId={}",e.userId());
    }
    @EventListener public void linked(MinorLinkedEvent e) {
        notify(e.tutorUserId(),"Menor vinculado","Se vinculó un menor bajo tu responsabilidad. Ya puedes gestionar sus citas.");
        LOG.info("IAM minor linked: userId={}, patientId={}",e.tutorUserId(),e.minorPatientId());
    }
    @EventListener public void unlinked(MinorUnlinkedEvent e) {
        notify(e.tutorUserId(),"Menor desvinculado","Se eliminó el vínculo con el menor. Sus datos de atención se conservaron.");
        LOG.info("IAM minor unlinked: userId={}, patientId={}",e.tutorUserId(),e.minorPatientId());
    }
    @EventListener public void updated(ProfileUpdatedEvent e) {
        notify(e.userId(),"Seguridad de tu cuenta","Se modificaron los datos de tu cuenta de SaludYa. Si no fuiste tú, contacta con admisión.");
        users.findById(e.userId()).filter(u->!u.getEmail().value().equals(e.previousEmail()))
                .ifPresent(u->notifications.enqueue(e.previousEmail(),"Seguridad de tu cuenta","El correo de tu cuenta SaludYa fue actualizado. Si no fuiste tú, contacta con admisión."));
        LOG.info("IAM profile updated: userId={}",e.userId());
    }
    @EventListener public void staffCreated(StaffAccountCreatedEvent e) { LOG.info("IAM staff created: userId={}",e.userId()); }
}
