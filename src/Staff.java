public class Staff extends Member {
    private final int maxItems = 4;
    private final int loanPeriod = 21;

    public Staff(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxItems() { return maxItems; }

    @Override
    public int getLoanPeriod() { return loanPeriod; }
}
