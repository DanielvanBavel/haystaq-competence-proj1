package nl.haystaq.tijdwijs.projecten.application;

import nl.haystaq.tijdwijs.projecten.domain.Client;

import java.util.UUID;

public record ClientView(
        UUID id,
        String name,
        String contactEmail,
        String vatNumber,
        String country,
        int paymentTermDays,
        boolean active) {

    public static ClientView from(Client client) {
        return new ClientView(client.id(), client.name(), client.contactEmail(), client.vatNumber(),
                client.country(), client.paymentTermDays(), client.isActive());
    }
}
