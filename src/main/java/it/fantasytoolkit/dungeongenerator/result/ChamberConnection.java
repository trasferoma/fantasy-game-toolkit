package it.fantasytoolkit.dungeongenerator.result;

public record ChamberConnection(int fromChamberId, int toChamberId) {

    public ChamberConnection(int fromChamberId, int toChamberId) {
        this.fromChamberId = Math.min(fromChamberId, toChamberId);
        this.toChamberId = Math.max(fromChamberId, toChamberId);
    }
}
