package com.compass.digitalbank.domain.port.out;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;

public interface NotificationPort {

    void notifyTransferCompleted(TransferCompletedEvent event);
}
