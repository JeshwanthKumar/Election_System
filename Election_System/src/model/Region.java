import java.util.List;

public class Region {
    String name;
    List<Contestant> contestants;
	public Vote[] votes;

    public Region(String name, List<Contestant> contestants) {
        this.name = name;
        this.contestants = contestants;
    }
}
