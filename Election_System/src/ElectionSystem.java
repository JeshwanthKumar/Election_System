import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ElectionSystem {
    public static void main(String[] args) throws FileNotFoundException {
        ElectionSystem electionSystem = new ElectionSystem();
        electionSystem.run();
    }

    private void run() throws FileNotFoundException {
        List<Contestant> contestants = initializeContestants();
        Map<String, Region> regions = initializeRegions(contestants);
        String filePath = "C:\\Users\\jeshn\\Downloads\\voting.dat.txt";
        List<Vote> votes = readVotesFromFile(filePath);
        // Process votes and determine winners
        processVotes(votes, regions);

        // Display results
        displayResults(regions, contestants);
    }

    private void processVotes(List<Vote> votes, Map<String, Region> regions) {
        // Initialize points for contestants
        Map<Contestant, Integer> contestantPoints = new HashMap<>();
        for (Region region : regions.values()) {
            for (Contestant contestant : region.contestants) {
                contestantPoints.put(contestant, 0);
            }
        }

        // Process each vote and update points
        for (Vote vote : votes) {
            // Check for invalid votes
            if (!isValidVote(vote, regions.get(vote.regionName))) {
                // Handle invalid vote
                System.out.println("Invalid Vote: " + vote.voterId);
                continue;
            }

            // Update points based on preferences
            int points = 3;
            for (Contestant preference : vote.preferences) {
                contestantPoints.put(preference, contestantPoints.get(preference) + points);
                points--;
            }
        }

        // Determine winners
        determineWinners(regions, contestantPoints);
    }

    private boolean isValidVote(Vote vote, Region region) {
        // Check if the voter has voted from the correct region
        if (region == null || !region.name.equals(vote.regionName)) {
            return false; // Invalid vote
        }

        // Check the number of preferences in the vote
        if (vote.preferences.size() < 1 || vote.preferences.size() > 3) {
            return false; // Invalid vote
        }

        // Check if all preferences are valid contestants in the region
        for (Contestant preference : vote.preferences) {
            if (!region.contestants.contains(preference)) {
                return false; // Invalid vote
            }
        }

        return true; // Valid vote
    }

    private void determineWinners(Map<String, Region> regions, Map<Contestant, Integer> contestantPoints) {
        // Determine Chief Officer (Overall winner)
        Contestant chiefOfficer = determineOverallWinner(contestantPoints);
        System.out.println("Chief Officer: " + chiefOfficer.name + " with " + contestantPoints.get(chiefOfficer) + " points.");

        // Determine Regional Heads
        for (Region region : regions.values()) {
            Contestant regionalHead = determineRegionalHead(region, contestantPoints);
            System.out.println("Regional Head for " + region.name + ": " + regionalHead.name + " with "
                    + contestantPoints.get(regionalHead) + " points.");
        }
    }

    private Contestant determineOverallWinner(Map<Contestant, Integer> contestantPoints) {
        Contestant overallWinner = null;
        int maxPoints = Integer.MIN_VALUE;

        for (Map.Entry<Contestant, Integer> entry : contestantPoints.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                overallWinner = entry.getKey();
            }
        }

        return overallWinner;
    }

    private Contestant determineRegionalHead(Region region, Map<Contestant, Integer> contestantPoints) {
        Contestant regionalHead = null;
        int maxPoints = Integer.MIN_VALUE;

        for (Contestant contestant : region.contestants) {
            int points = contestantPoints.getOrDefault(contestant, 0);
            if (points > maxPoints) {
                maxPoints = points;
                regionalHead = contestant;
            }
        }

        return regionalHead;
    }

    private void displayResults(Map<String, Region> regions, List<Contestant> contestants) {
        // Display overall winner
        Map<Contestant, Integer> overallPoints = calculateOverallPoints(regions);
        Contestant overallWinner = determineOverallWinner(overallPoints);
        System.out.println("Overall Winner: " + overallWinner.name);

        // Display regional heads for each region
        for (Region region : regions.values()) {
            Map<Contestant, Integer> regionalPoints = calculateRegionalPoints(region, contestants);
            Contestant regionalHead = determineRegionalHead(region, regionalPoints);
            System.out.println("Regional Head for " + region.name + ": " + regionalHead.name);
        }
    }

    private Map<Contestant, Integer> calculateOverallPoints(Map<String, Region> regions) {
        // Calculate overall points for each contestant across all regions
        Map<Contestant, Integer> overallPoints = new HashMap<>();

        for (Region region : regions.values()) {
            Map<Contestant, Integer> regionalPoints = calculateRegionalPoints(region, region.contestants);
            for (Map.Entry<Contestant, Integer> entry : regionalPoints.entrySet()) {
                overallPoints.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        return overallPoints;
    }

    private Map<Contestant, Integer> calculateRegionalPoints(Region region, List<Contestant> contestants) {
        // Calculate points for each contestant in a specific region
        Map<Contestant, Integer> regionalPoints = new HashMap<>();

        for (Vote vote : region.votes) {
            for (int i = 0; i < vote.preferences.size(); i++) {
                Contestant contestant = vote.preferences.get(i);
                int points = (3 - i); // Assign points based on preference
                regionalPoints.merge(contestant, points, Integer::sum);
            }
        }

        return regionalPoints;
    }

    private List<Contestant> initializeContestants() {
        List<Contestant> contestants = new ArrayList<>();
        for (char c = 'A'; c <= 'Y'; c++) {
            contestants.add(new Contestant(String.valueOf(c)));
        }
        return contestants;
    }

    private Map<String, Region> initializeRegions(List<Contestant> contestants) {
        Map<String, Region> regions = new HashMap<>();

        // Statically provide regions, you can replace this with reading from an input file
        List<String> regionDetails = List.of(
                "Khammam/ABFMNTRY",
                "Visakhapatnam/BCGLKP"
                // Add more regions as needed
        );

        for (String regionDetail : regionDetails) {
            String[] parts = regionDetail.split("/");
            String regionName = parts[0];
            String contestantList = parts[1];

            List<Contestant> regionContestants = new ArrayList<>();
            for (char c : contestantList.toCharArray()) {
                Contestant contestant = contestants.stream()
                        .filter(cont -> cont.name.equals(String.valueOf(c)))
                        .findFirst()
                        .orElseThrow(); // Adjust error handling as needed
                regionContestants.add(contestant);
            }

            Region region = new Region(regionName, regionContestants);
            regions.put(regionName, region);
        }

        return regions;
    }

    private List<Vote> readVotesFromFile(String filename) throws FileNotFoundException {
        List<Vote> votes = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNextLine()) {
            String regionName = scanner.nextLine().trim();
            if ("&&".equals(regionName)) {
                break; // End of input file
            }

            List<Vote> regionVotes = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals("//")) {
                    break;
                }

                String[] parts = line.split("\\s+");
                String voterId = parts[0];
                List<Contestant> preferences = parsePreferences(parts, 1);

                regionVotes.add(new Vote(regionName, voterId, preferences));
            }

            votes.addAll(regionVotes);
        }

        scanner.close();
        return votes;
    }

    private List<Contestant> parsePreferences(String[] parts, int startIndex) {
        List<Contestant> preferences = new ArrayList<>();
        for (int i = startIndex; i < parts.length; i++) {
            char contestantChar = parts[i].charAt(0);
            Contestant contestant = new Contestant(String.valueOf(contestantChar));
            preferences.add(contestant);
        }
        return preferences;
    }
}
