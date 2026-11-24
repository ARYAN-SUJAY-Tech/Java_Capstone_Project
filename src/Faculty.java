public class Faculty extends Member {
    private final int maxItems = 5;
    private final int loanPeriod = 30;

    public Faculty(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxItems() { return maxItems; }

    @Override
    public int getLoanPeriod() { return loanPeriod; }
}
