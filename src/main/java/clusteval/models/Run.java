package clusteval;

public class Run implements Comparable<Run> {
    private String name;

    private String displayName;

    private String date;

    boolean edited = false;

    public Run () {}

    public Run (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public int compareTo(Run other) {
        return getName().compareTo(other.getName());
    }
}
