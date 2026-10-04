package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueuePosition;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueuePositionResource;

/**
 * Assembler from {@link QueuePosition} to {@link QueuePositionResource}.
 */
public final class QueuePositionResourceAssembler {

    private QueuePositionResourceAssembler() {
    }

    public static QueuePositionResource toResource(QueuePosition position) {
        return new QueuePositionResource(position.value(), position.totalInQueue());
    }
}
