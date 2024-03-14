import java.util.List;

public class Vote {
    String regionName;
    String voterId;
    List<Contestant> preferences;

    public Vote(String regionName, String voterId, List<Contestant> preferences) {
        this.regionName = regionName;
        this.voterId = voterId;
        this.preferences = preferences;
    }
}
