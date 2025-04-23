package com.gabriaum.change.vote;

import com.gabriaum.change.ChangeMain;
import com.gabriaum.change.type.ChangeType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Vote {

    private ChangeType actualType, expectedType;

    private int count, deadlineCount;

    private boolean voting, automatic;

    private final Set<UUID> accepted, denied;

    public Vote(ChangeType actualType) {
        this.actualType = actualType;
        this.expectedType = ChangeType.getNext(actualType);
        this.accepted = new HashSet<>();
        this.denied = new HashSet<>();
        this.count = 0;
        this.deadlineCount = 0;
        this.voting = false;
    }

    public void addAccepted(UUID uuid) {
        accepted.add(uuid);
    }

    public void addDenied(UUID uuid) {
        denied.add(uuid);
    }

    public void removeAccepted(UUID uuid) {
        accepted.remove(uuid);
    }

    public void removeDenied(UUID uuid) {
        denied.remove(uuid);
    }

    public boolean isAccepted() {
        return getAccept() > getDeny();
    }

    public boolean isUnanimous() {
        return getAccept() == getDeny();
    }

    public int getAccept() {
        return accepted.size();
    }

    public int getDeny() {
        return denied.size();
    }
}